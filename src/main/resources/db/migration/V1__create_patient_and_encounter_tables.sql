CREATE TABLE patients (
    id BIGINT NOT NULL AUTO_INCREMENT,
    medical_record_number VARCHAR(64) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    date_of_birth DATE NOT NULL,
    email VARCHAR(320) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_patients_mrn UNIQUE (medical_record_number)
);

CREATE TABLE encounters (
    id BIGINT NOT NULL AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    encounter_date DATE NOT NULL,
    type VARCHAR(100) NOT NULL,
    clinician VARCHAR(200) NOT NULL,
    notes VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_encounters_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE INDEX idx_encounters_patient_date ON encounters (patient_id, encounter_date, id);
