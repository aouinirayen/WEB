-- Script de test pour le système de notation
-- Exécutez ce script dans phpMyAdmin ou MySQL Workbench

-- 1. Créer un covoiturage de test
INSERT INTO covoiturage (
    driver_name, 
    departure, 
    destination, 
    date, 
    heure_depart, 
    heure_arrivee, 
    price, 
    available_seats, 
    vehicle, 
    status,
    created_at,
    updated_at
) VALUES (
    'TestDriver', 
    'Tunis', 
    'Sousse', 
    '2026-04-30', 
    '08:00', 
    '10:00', 
    15, 
    4, 
    'Toyota', 
    'CONFIRMED',
    NOW(),
    NOW()
);

-- 2. Créer une réservation confirmée pour votre email
-- Remplacez 'VOTRE_NOM' par votre vrai nom
INSERT INTO reservation (
    client_name, 
    phone, 
    email, 
    seats_reserved, 
    booking_date, 
    status, 
    covoiturage_id,
    created_at,
    updated_at
) VALUES (
    'VOTRE_NOM', 
    '21612345678', 
    'bouderbalamedamine712@gmail.com', 
    1, 
    '2026-04-30', 
    'CONFIRMED', 
    LAST_INSERT_ID(),
    NOW(),
    NOW()
);

-- 3. Vérifier les données créées
SELECT 
    c.id as covoiturage_id,
    c.driver_name,
    c.departure,
    c.destination,
    c.date,
    r.client_name,
    r.email,
    r.status as reservation_status
FROM covoiturage c
JOIN reservation r ON c.id = r.covoiturage_id
WHERE r.email = 'bouderbalamedamine712@gmail.com'
AND r.status = 'CONFIRMED';
