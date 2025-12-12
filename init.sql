CREATE SCHEMA IF NOT EXISTS user_service;
CREATE SCHEMA IF NOT EXISTS order_service;

GRANT ALL ON SCHEMA user_service TO postgres;
GRANT ALL ON SCHEMA order_service TO postgres;