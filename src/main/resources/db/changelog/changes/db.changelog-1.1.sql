--liquibase formatted sql

--changeset user_service:3
INSERT INTO users (name, user_id, surname, birth_date, email, active)
VALUES
    ('Admin', 1, 'Admin', '1990-04-12', 'admin@example.com', 'ACTIVE'),
    ('Nikita', 2,'Yatsko', '1985-09-22', 'nikitayatsko@example.com', 'ACTIVE');


--changeset user_service:4
INSERT INTO payment_cards (user_id, number, holder, expiration_date, active)
VALUES
    (1, '1111222233334444', 'Admin Admin', '2027-04-30', 'ACTIVE'),
    (1, '5555666677778888', 'Admin Admin', '2026-09-30', 'ACTIVE'),
    (2, '9999000011112222', 'Nikita Nikita', '2028-01-31', 'ACTIVE'),
    (2, '3333444455556666', 'Nikita Nikita', '2025-12-31', 'ACTIVE');