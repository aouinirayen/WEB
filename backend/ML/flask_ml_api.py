from flask import Flask, request, jsonify
import joblib
import numpy as np

app = Flask(__name__)

# Chargement des modèles
churn_model  = joblib.load('models/churn_model.pkl')
clv_model    = joblib.load('models/clv_model.pkl')
rec_model    = joblib.load('models/recommend_model.pkl')
action_model = joblib.load('models/action_model.pkl')

scaler_churn = joblib.load('models/scaler_churn.pkl')
scaler_clv   = joblib.load('models/scaler_clv.pkl')
scaler_rec   = joblib.load('models/scaler_rec.pkl')
scaler_act   = joblib.load('models/scaler_act.pkl')

le_plan      = joblib.load('models/le_plan.pkl')
le_action    = joblib.load('models/le_action.pkl')
le_transport = joblib.load('models/le_transport.pkl')
le_plan_type = joblib.load('models/le_plan_type.pkl')
le_loyalty   = joblib.load('models/le_loyalty.pkl')

PLAN_LABELS   = list(le_plan.classes_)
ACTION_LABELS = list(le_action.classes_)


def encode_features(data):
    """Encode les features catégorielles reçues depuis Spring Boot."""
    transport_enc = int(le_transport.transform([data['transport_type']])[0])
    plan_enc      = int(le_plan_type.transform([data['plan_type']])[0])
    loyalty_enc   = int(le_loyalty.transform([data['niveau_loyalty']])[0])
    return transport_enc, plan_enc, loyalty_enc


def compute_derived(data, transport_enc, plan_enc, loyalty_enc):
    """Recalcule les features dérivées comme dans le pipeline."""
    nb_abs = max(data['nb_abonnements'], 1)
    ratio_fidelite  = data['nb_renouvellements'] / nb_abs
    revenu_par_mois = data['montant_total_paye'] / max(data['anciennete_jours'] / 30, 1)
    points_par_sub  = data['points_cumules'] / nb_abs
    est_inactif     = int(data['a_abonnement_actif'] == 0 and data['anciennete_jours'] > 90)

    taux_annulation  = data['nb_annulations'] / nb_abs
    inactivite_pts   = 1.0 - min(data['points_cumules'] / 600.0, 1.0)
    expire_sans_renew= data.get('expire_sans_renew', 0)
    courte_duree     = 1.0 - min(data['duree_moy_jours'] / 360.0, 1.0)

    # Score recommandation (reproduit ton MLServiceImpl)
    s1 = min(data['nb_abonnements'] / 10.0, 1.0)
    s2 = min(data['prix_moyen'] / 50.0, 1.0)
    s3 = min(data['points_cumules'] / 600.0, 1.0)
    s4 = 1.0 - min(data['nb_annulations'] / 5.0, 1.0)
    score_rec = (s1*0.30) + (s2*0.35) + (s3*0.25) + (s4*0.10)

    return {
        'taux_annulation': taux_annulation,
        'inactivite_points': inactivite_pts,
        'expire_sans_renew': expire_sans_renew,
        'courte_duree': courte_duree,
        'ratio_fidelite': ratio_fidelite,
        'revenu_par_mois': revenu_par_mois,
        'points_par_sub': points_par_sub,
        'est_inactif': est_inactif,
        'score_recommandation': score_rec,
        'transport_enc': transport_enc,
        'plan_enc': plan_enc,
        'loyalty_enc': loyalty_enc,
    }


@app.route('/predict/all', methods=['POST'])
def predict_all():
    """Endpoint principal — appel unique depuis Spring Boot."""
    data = request.json
    transport_enc, plan_enc, loyalty_enc = encode_features(data)
    d = compute_derived(data, transport_enc, plan_enc, loyalty_enc)

    # ── Churn ──
    x_churn = scaler_churn.transform([[
        data['jours_restants'], d['taux_annulation'], d['inactivite_points'],
        d['expire_sans_renew'], d['courte_duree'], data['points_cumules'],
        data['nb_abonnements'], data['anciennete_jours'], plan_enc,
        d['ratio_fidelite'], d['est_inactif'], data['nb_transactions']
    ]])
    churn_prob  = float(churn_model.predict_proba(x_churn)[0][1])
    churn_label = int(churn_model.predict(x_churn)[0])

    # Remplace dans /predict/all
    if churn_prob >= 0.70:
        risk_level = 'HIGH'       # était 'ÉLEVÉ'
    elif churn_prob >= 0.40:
        risk_level = 'MODERATE'   # était 'MODÉRÉ'
    else:
        risk_level = 'LOW'        # était 'FAIBLE'

    # ── CLV ──
    x_clv = scaler_clv.transform([[
        data['nb_renouvellements'], data['prix_moyen'], data['anciennete_jours'],
        data['points_cumules'], loyalty_enc, plan_enc, transport_enc,
        d['taux_annulation'], data['duree_moy_jours'], d['ratio_fidelite'],
        d['revenu_par_mois'], d['points_par_sub'], data['nb_transactions']
    ]])
    clv_value = float(clv_model.predict(x_clv)[0])

    # ── Recommandation plan ──
    x_rec = scaler_rec.transform([[
        data['nb_abonnements'], data['prix_moyen'], data['points_cumules'],
        data['nb_annulations'], data['anciennete_jours'], transport_enc,
        d['ratio_fidelite'], d['revenu_par_mois'], d['points_par_sub'],
        loyalty_enc, data['duree_moy_jours']
    ]])
    rec_idx  = int(rec_model.predict(x_rec)[0])
    rec_plan = PLAN_LABELS[rec_idx]
    rec_proba= float(max(rec_model.predict_proba(x_rec)[0]))

    # ── Action ──
    x_act = scaler_act.transform([[
        data['jours_restants'], d['taux_annulation'], d['inactivite_points'],
        d['expire_sans_renew'], d['courte_duree'], data['points_cumules'],
        data['nb_abonnements'], data['anciennete_jours'], plan_enc,
        d['ratio_fidelite'], d['est_inactif'], data['nb_transactions'],
        d['score_recommandation'], plan_enc
    ]])
    action_idx = int(action_model.predict(x_act)[0])
    action     = ACTION_LABELS[action_idx]

    return jsonify({
        'churn': {
            'probability': round(churn_prob, 3),
            'label':       churn_label,
            'riskLevel':   risk_level
        },
        'clv': {
            'value':    round(max(clv_value, 0), 2),
            'currency': 'DT'
        },
        'recommendation': {
            'plan':       rec_plan,
            'confidence': round(rec_proba * 100, 1)
        },
        'action': action
    })


@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok', 'models': 4})


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)