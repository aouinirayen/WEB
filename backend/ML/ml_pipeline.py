import pandas as pd
import numpy as np
import joblib
import os
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.ensemble import RandomForestClassifier, GradientBoostingRegressor
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (classification_report, confusion_matrix,
                             mean_absolute_error, r2_score)
import warnings
warnings.filterwarnings('ignore')

# ─── 0. Chargement ───────────────────────────────────────
df = pd.read_csv('transit_dataset.csv')
print(f"Shape : {df.shape}")
print(df.isnull().sum())

# ─── 1. NETTOYAGE ─────────────────────────────────────────
# Aucune valeur nulle (dataset synthétique contrôlé)
# Clamp des valeurs aberrantes
df['prix_moyen']       = df['prix_moyen'].clip(0, 150)
df['points_cumules']   = df['points_cumules'].clip(0, 1000)
df['anciennete_jours'] = df['anciennete_jours'].clip(0, 1500)
df['clv_12mois']       = df['clv_12mois'].clip(0, 2000)

# ─── 2. FEATURE ENGINEERING ───────────────────────────────
# Encodage catégoriel
le_transport = LabelEncoder()
le_plan      = LabelEncoder()
le_loyalty   = LabelEncoder()
le_action    = LabelEncoder()
le_rec_plan  = LabelEncoder()

df['transport_enc'] = le_transport.fit_transform(df['transport_type'])
df['plan_enc']      = le_plan.fit_transform(df['plan_type'])
df['loyalty_enc']   = le_loyalty.fit_transform(df['niveau_loyalty'])

# Nouvelles features dérivées
df['ratio_fidelite']   = df['nb_renouvellements'] / df['nb_abonnements'].clip(1)
df['revenu_par_mois']  = df['montant_total_paye'] / (df['anciennete_jours'] / 30).clip(1)
df['points_par_sub']   = df['points_cumules'] / df['nb_abonnements'].clip(1)
df['est_inactif']      = ((df['a_abonnement_actif'] == 0) & (df['anciennete_jours'] > 90)).astype(int)

print("\nFeatures après engineering :")
print(df[['ratio_fidelite','revenu_par_mois','points_par_sub','est_inactif']].describe())

# ─── 3. FEATURES PAR MODÈLE ───────────────────────────────
FEATURES_CHURN = [
    'jours_restants', 'taux_annulation', 'inactivite_points',
    'expire_sans_renew', 'courte_duree', 'points_cumules',
    'nb_abonnements', 'anciennete_jours', 'plan_enc',
    'ratio_fidelite', 'est_inactif', 'nb_transactions'
]

FEATURES_CLV = [
    'nb_renouvellements', 'prix_moyen', 'anciennete_jours',
    'points_cumules', 'loyalty_enc', 'plan_enc', 'transport_enc',
    'taux_annulation', 'duree_moy_jours', 'ratio_fidelite',
    'revenu_par_mois', 'points_par_sub', 'nb_transactions'
]

FEATURES_RECOMMEND = [
    'nb_abonnements', 'prix_moyen', 'points_cumules',
    'nb_annulations', 'anciennete_jours', 'transport_enc',
    'ratio_fidelite', 'revenu_par_mois', 'points_par_sub',
    'loyalty_enc', 'duree_moy_jours'
]

FEATURES_ACTION = FEATURES_CHURN + ['score_recommandation', 'plan_enc']


# ─── 4. MODÈLE 1 : SMART CHURN PREDICTOR ─────────────────
print("\n" + "="*50)
print("MODÈLE 1 — Smart Churn Predictor")
print("="*50)

X_c = df[FEATURES_CHURN]
y_c = df['churn']
X_tr, X_te, y_tr, y_te = train_test_split(X_c, y_c, test_size=0.2, random_state=42, stratify=y_c)

scaler_churn = StandardScaler()
X_tr_s = scaler_churn.fit_transform(X_tr)
X_te_s = scaler_churn.transform(X_te)

churn_model = RandomForestClassifier(
    n_estimators=150, max_depth=8,
    min_samples_leaf=4, random_state=42, class_weight='balanced'
)
churn_model.fit(X_tr_s, y_tr)

y_pred = churn_model.predict(X_te_s)
print(classification_report(y_te, y_pred, target_names=['No Churn','Churn']))

cv = cross_val_score(churn_model, scaler_churn.transform(X_c), y_c, cv=5, scoring='f1')
print(f"CV F1 (5-fold) : {cv.mean():.3f} ± {cv.std():.3f}")

# Importance des features
fi = pd.Series(churn_model.feature_importances_, index=FEATURES_CHURN).sort_values(ascending=False)
print("\nTop features churn :")
print(fi.head(6))


# ─── 5. MODÈLE 2 : CUSTOMER LIFETIME VALUE ───────────────
print("\n" + "="*50)
print("MODÈLE 2 — Customer Lifetime Value")
print("="*50)

X_v = df[FEATURES_CLV]
y_v = df['clv_12mois']
X_tr, X_te, y_tr, y_te = train_test_split(X_v, y_v, test_size=0.2, random_state=42)

scaler_clv = StandardScaler()
X_tr_s = scaler_clv.fit_transform(X_tr)
X_te_s = scaler_clv.transform(X_te)

clv_model = GradientBoostingRegressor(
    n_estimators=200, max_depth=5,
    learning_rate=0.08, subsample=0.85,
    random_state=42
)
clv_model.fit(X_tr_s, y_tr)

y_pred = clv_model.predict(X_te_s)
print(f"MAE  : {mean_absolute_error(y_te, y_pred):.2f} DT")
print(f"R²   : {r2_score(y_te, y_pred):.3f}")

fi_clv = pd.Series(clv_model.feature_importances_, index=FEATURES_CLV).sort_values(ascending=False)
print("\nTop features CLV :")
print(fi_clv.head(6))


# ─── 6. MODÈLE 3 : RECOMMANDATION DE PLAN ─────────────────
print("\n" + "="*50)
print("MODÈLE 3 — Plan Recommendation")
print("="*50)

X_r = df[FEATURES_RECOMMEND]
y_r = le_rec_plan.fit_transform(df['recommended_plan'])
X_tr, X_te, y_tr, y_te = train_test_split(X_r, y_r, test_size=0.2, random_state=42, stratify=y_r)

scaler_rec = StandardScaler()
X_tr_s = scaler_rec.fit_transform(X_tr)
X_te_s = scaler_rec.transform(X_te)

rec_model = RandomForestClassifier(
    n_estimators=150, max_depth=10,
    min_samples_leaf=3, random_state=42
)
rec_model.fit(X_tr_s, y_tr)
y_pred = rec_model.predict(X_te_s)
print(classification_report(y_te, y_pred, target_names=le_rec_plan.classes_))


# ─── 7. MODÈLE 4 : ACTION RECOMMANDÉE ────────────────────
print("\n" + "="*50)
print("MODÈLE 4 — Action Recommandée")
print("="*50)

X_a = df[FEATURES_ACTION]
y_a = le_action.fit_transform(df['action_recommandee'])
X_tr, X_te, y_tr, y_te = train_test_split(X_a, y_a, test_size=0.2, random_state=42, stratify=y_a)

scaler_act = StandardScaler()
X_tr_s = scaler_act.fit_transform(X_tr)
X_te_s = scaler_act.transform(X_te)

action_model = RandomForestClassifier(
    n_estimators=200, max_depth=10,
    min_samples_leaf=3, random_state=42
)
action_model.fit(X_tr_s, y_tr)
y_pred = action_model.predict(X_te_s)
print(classification_report(y_te, y_pred, target_names=le_action.classes_))


# ─── 8. SAUVEGARDE ────────────────────────────────────────
os.makedirs('models', exist_ok=True)

joblib.dump(churn_model,    'models/churn_model.pkl')
joblib.dump(clv_model,      'models/clv_model.pkl')
joblib.dump(rec_model,      'models/recommend_model.pkl')
joblib.dump(action_model,   'models/action_model.pkl')
joblib.dump(scaler_churn,   'models/scaler_churn.pkl')
joblib.dump(scaler_clv,     'models/scaler_clv.pkl')
joblib.dump(scaler_rec,     'models/scaler_rec.pkl')
joblib.dump(scaler_act,     'models/scaler_act.pkl')
joblib.dump(le_rec_plan,    'models/le_plan.pkl')
joblib.dump(le_action,      'models/le_action.pkl')
joblib.dump(le_transport,   'models/le_transport.pkl')
joblib.dump(le_plan,        'models/le_plan_type.pkl')
joblib.dump(le_loyalty,     'models/le_loyalty.pkl')

print("\nModèles sauvegardés dans /models/")