import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import random

np.random.seed(42)
N = 800  # 800 passagers

def random_date(start, end):
    return start + timedelta(days=random.randint(0, (end - start).days))

start = datetime(2022, 1, 1)
end   = datetime(2024, 12, 31)

rows = []
for passenger_id in range(1, N + 1):

    # Profil passager
    anciennete_jours  = random.randint(30, 900)
    nb_abonnements    = random.randint(1, 8)
    nb_annulations    = random.randint(0, min(nb_abonnements, 3))
    nb_renouvellements= nb_abonnements - nb_annulations
    transport_type    = random.choice(['BUS', 'METRO', 'TRAIN', 'FERRY', 'TRAMWAY'])
    plan_type         = random.choices(['FREE', 'BASIC', 'PREMIUM'], weights=[0.3, 0.45, 0.25])[0]
    prix_moyen        = {'FREE': 0, 'BASIC': 29.0, 'PREMIUM': 59.0}[plan_type] + random.uniform(-5, 5)
    prix_moyen        = max(0, prix_moyen)
    duree_moy_jours   = random.randint(30, 365)
    
    # Loyalty
    points_cumules    = random.randint(0, 800)
    niveau_loyalty    = 'GOLD' if points_cumules >= 500 else ('SILVER' if points_cumules >= 200 else 'BRONZE')
    nb_transactions   = random.randint(0, 30)
    
    # Abonnement actif
    a_abonnement_actif = random.choice([True, False])
    jours_restants     = random.randint(0, 365) if a_abonnement_actif else 0
    
    # Paiements
    montant_total_paye = nb_renouvellements * prix_moyen
    
    # Features churn dérivées (imitent ton MLServiceImpl)
    taux_annulation   = nb_annulations / max(nb_abonnements, 1)
    inactivite_points = 1.0 - min(points_cumules / 600.0, 1.0)
    expire_sans_renew = 1 if (nb_annulations > 0 and not a_abonnement_actif) else 0
    courte_duree      = 1.0 - min(duree_moy_jours / 360.0, 1.0)
    
    # ── Label CHURN (logique cohérente avec ton sigmoid) ──
    # On reprend exactement ta formule pour labelliser
    f1 = min(jours_restants / 365.0, 1.0)
    f2 = taux_annulation
    f3 = inactivite_points
    f4 = float(expire_sans_renew)
    f5 = courte_duree
    z  = -1.5 + (-2.0*f1) + (2.5*f2) + (1.8*f3) + (2.0*f4) + (1.2*f5)
    p  = 1.0 / (1.0 + np.exp(-z))
    # Label bruité (±5% pour rendre le dataset réaliste)
    churn = 1 if (p + random.uniform(-0.05, 0.05)) >= 0.5 else 0
    churn_prob = round(p, 3)
    
    # ── Label PLAN RECOMMANDÉ (cohérent avec ton scoring pondéré) ──
    s1  = min(nb_abonnements / 10.0, 1.0)
    s2  = min(prix_moyen / 50.0, 1.0)
    s3  = min(points_cumules / 600.0, 1.0)
    s4  = 1.0 - min(nb_annulations / 5.0, 1.0)
    score = (s1*0.30) + (s2*0.35) + (s3*0.25) + (s4*0.10)
    if score >= 0.65:
        recommended_plan = 'PREMIUM'
    elif score >= 0.35:
        recommended_plan = 'BASIC'
    else:
        recommended_plan = 'FREE'
    
    # ── Label CLV — Customer Lifetime Value (en DT sur 12 mois) ──
    # Base réaliste : renouvellements futurs estimés × prix
    mois_restants = min(anciennete_jours / 30, 12)
    clv = round(
        nb_renouvellements * prix_moyen * (1 - taux_annulation * 0.5)
        + points_cumules * 0.05
        + mois_restants * prix_moyen * 0.3
        + random.gauss(0, 5),  # bruit
        2
    )
    clv = max(0, clv)
    
    # ── Label ACTION RECOMMANDÉE ──
    if churn == 1 and plan_type == 'PREMIUM':
        action = 'OFFRE_RETENTION_PREMIUM'
    elif churn == 1:
        action = 'CODE_PROMO_URGENCE'
    elif score >= 0.65 and plan_type != 'PREMIUM':
        action = 'PROPOSER_UPGRADE_PREMIUM'
    elif score >= 0.35 and plan_type == 'FREE':
        action = 'PROPOSER_UPGRADE_BASIC'
    elif points_cumules < 100 and nb_abonnements > 2:
        action = 'BOOST_POINTS_LOYALTY'
    else:
        action = 'MAINTENIR_ENGAGEMENT'
    
    rows.append({
        # Identifiant
        'passenger_id':        passenger_id,
        # Features brutes
        'nb_abonnements':      nb_abonnements,
        'nb_annulations':      nb_annulations,
        'nb_renouvellements':  nb_renouvellements,
        'anciennete_jours':    anciennete_jours,
        'transport_type':      transport_type,
        'plan_type':           plan_type,
        'prix_moyen':          round(prix_moyen, 2),
        'duree_moy_jours':     duree_moy_jours,
        'montant_total_paye':  round(montant_total_paye, 2),
        'points_cumules':      points_cumules,
        'niveau_loyalty':      niveau_loyalty,
        'nb_transactions':     nb_transactions,
        'a_abonnement_actif':  int(a_abonnement_actif),
        'jours_restants':      jours_restants,
        # Features dérivées (feature engineering déjà fait)
        'taux_annulation':     round(taux_annulation, 3),
        'inactivite_points':   round(inactivite_points, 3),
        'expire_sans_renew':   expire_sans_renew,
        'courte_duree':        round(courte_duree, 3),
        'score_recommandation':round(score, 3),
        # Labels
        'churn':               churn,
        'churn_prob':          churn_prob,
        'recommended_plan':    recommended_plan,
        'clv_12mois':          clv,
        'action_recommandee':  action,
    })

df = pd.DataFrame(rows)
df.to_csv('transit_dataset.csv', index=False)
print(f"Dataset créé : {df.shape}")
print(df.head(3))
print("\nDistribution churn :", df['churn'].value_counts().to_dict())
print("Distribution plan   :", df['recommended_plan'].value_counts().to_dict())
print("Distribution action :", df['action_recommandee'].value_counts().to_dict())