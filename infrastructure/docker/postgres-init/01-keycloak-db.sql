-- Create keycloak database for Keycloak's internal storage
CREATE DATABASE keycloak;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO threadly;
