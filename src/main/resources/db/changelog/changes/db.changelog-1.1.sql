--liquibase formatted sql

--changeset nyatska:3
INSERT INTO users (name, surname, birth_date, email, active)
VALUES
    ('Alice', 'Johnson', '1990-04-12', 'alice@example.com', 'ACTIVE'),
    ('Bob', 'Smith', '1985-09-22', 'bob@example.com', 'ACTIVE'),
    ('Charlie', 'Brown', '1995-01-15', 'charlie@example.com', 'ACTIVE');


--changeset nyatska:4
INSERT INTO payment_cards (user_id, number, holder, expiration_date, active)
VALUES
    (1, '1111222233334444', 'Alice Johnson', '2027-04-30', 'ACTIVE'),
    (1, '5555666677778888', 'Alice Johnson', '2026-09-30', 'ACTIVE'),
    (2, '9999000011112222', 'Bob Smith', '2028-01-31', 'ACTIVE'),
    (3, '3333444455556666', 'Charlie Brown', '2025-12-31', 'ACTIVE');