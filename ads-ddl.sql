

-- Address
CREATE TABLE IF NOT EXISTS Addresses (
    address_id SERIAL PRIMARY KEY,
    address_line VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL
);

-- Dentist
CREATE TABLE IF NOT EXISTS Dentists (
    dentist_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(100) UNIQUE,
    specialization VARCHAR(100) NOT NULL
);

-- Patient
CREATE TABLE IF NOT EXISTS Patients (
    patient_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(100) UNIQUE,
    address_id INT UNIQUE NOT NULL,
    date_of_birth DATE NOT NULL,
    has_unpaid_bill BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_patient_address
        FOREIGN KEY (address_id)
        REFERENCES Addresses(address_id)
);

-- Surgery
CREATE TABLE IF NOT EXISTS Surgeries (
    surgery_id SERIAL PRIMARY KEY,
    surgery_name VARCHAR(100) NOT NULL,
    address_id INT UNIQUE NOT NULL,
    telephone_number VARCHAR(20),
    CONSTRAINT fk_surgery_address
        FOREIGN KEY (address_id)
        REFERENCES Addresses(address_id)
);

-- Appointment
CREATE TABLE IF NOT EXISTS Appointments (
    appointment_id SERIAL PRIMARY KEY,
    patient_id INT NOT NULL,
    dentist_id INT NOT NULL,
    surgery_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,

    CONSTRAINT fk_appointment_patient
        FOREIGN KEY (patient_id)
        REFERENCES Patients(patient_id),

    CONSTRAINT fk_appointment_dentist
        FOREIGN KEY (dentist_id)
        REFERENCES Dentists(dentist_id),

    CONSTRAINT fk_appointment_surgery
        FOREIGN KEY (surgery_id)
        REFERENCES Surgeries(surgery_id),

    CONSTRAINT uq_dentist_appointment
        UNIQUE (dentist_id, appointment_date, appointment_time)
);

-- Roles
CREATE TABLE IF NOT EXISTS Roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

-- Users
CREATE TABLE IF NOT EXISTS Users (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    dentist_id INT,
    patient_id INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_user_dentist
        FOREIGN KEY (dentist_id)
        REFERENCES Dentists(dentist_id),
    CONSTRAINT fk_user_patient
        FOREIGN KEY (patient_id)
        REFERENCES Patients(patient_id)
);

-- User Roles (Many-to-Many mapping)
CREATE TABLE IF NOT EXISTS UserRoles (
    user_role_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id)
        REFERENCES Users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id)
        REFERENCES Roles(role_id) ON DELETE CASCADE,
    CONSTRAINT uq_user_role
        UNIQUE (user_id, role_id)
);

-- Refresh Tokens
CREATE TABLE IF NOT EXISTS RefreshTokens (
    token_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
        REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Insert default roles
INSERT INTO Roles (role_name) VALUES 
    ('ROLE_PATIENT'),
    ('ROLE_DENTIST'),
    ('ROLE_OFFICE_MANAGER')
ON CONFLICT DO NOTHING;
