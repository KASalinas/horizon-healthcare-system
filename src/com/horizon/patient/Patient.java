package com.horizon.patient;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medical_record_number", nullable = false, unique = true, length = 64)
    private String medicalRecordNumber;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 320)
    private String email;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("date ASC, id ASC")
    private List<Encounter> encounters = new ArrayList<>();

    protected Patient() {
    }

    public Patient(String medicalRecordNumber, String fullName,
                   LocalDate dateOfBirth, String email) {
        this.medicalRecordNumber = medicalRecordNumber;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
    }

    public void update(String fullName, LocalDate dateOfBirth, String email) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
    }

    public void addEncounter(Encounter encounter) {
        encounter.assignTo(this);
        encounters.add(encounter);
    }

    public Long getId() {
        return id;
    }

    public String getMedicalRecordNumber() {
        return medicalRecordNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public List<Encounter> getEncounters() {
        return Collections.unmodifiableList(encounters);
    }
}
