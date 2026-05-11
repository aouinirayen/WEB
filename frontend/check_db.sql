-- Vérification des données pour le système de notation
-- Exécutez cette requête dans phpMyAdmin pour vérifier si les données existent

-- 1. Vérifier si vous avez des réservations confirmées
SELECT 
    r.id as reservation_id,
    r.client_name,
    r.email,
    r.status,
    r.covoiturage_id,
    c.id as covoiturage_exists,
    c.driver_name,
    c.departure,
    c.destination,
    c.date
FROM reservation r
LEFT JOIN covoiturage c ON r.covoiturage_id = c.id
WHERE r.email = 'bouderbalamedamine712@gmail.com'
AND r.status = 'CONFIRMED';

-- 2. Si aucune réservation confirmée, créer des données de test
INSERT INTO covoiturage (
    driver_name, departure, destination, date, heure_depart, heure_arrivee, 
    price, available_seats, vehicle, status, created_at, updated_at
) VALUES (
    'TestDriver', 'Tunis', 'Sousse', '2026-04-26', '08:00', '10:00', 
    15, 4, 'Toyota', 'CONFIRMED', NOW(), NOW()
);

-- Insérer une réservation confirmée pour votre nom exact
INSERT INTO reservation (
    client_name, phone, email, seats_reserved, booking_date, status, 
    covoiturage_id, created_at, updated_at
) VALUES (
    'Bouderbala Med Amine', '21612345678', 'bouderbalamedamine712@gmail.com', 
    1, '2026-04-26', 'CONFIRMED', LAST_INSERT_ID(), NOW(), NOW()
);
