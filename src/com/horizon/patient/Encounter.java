package com.horizon.patient;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "encounters")
public class Encounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false, length = 200)
    private String clinician;

    @Column(nullable = false, length = 4000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    protected Encounter() {
    }

    public Encounter(LocalDate date, String type, String clinician, String notes) {
        this.date = date;
        this.type = type;
        this.clinician = clinician;
        this.notes = notes == null ? "" : notes;
    }

    void assignTo(Patient patient) {
        this.patient = patient;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getClinician() {
        return clinician;
    }

    public String getNotes() {
        return notes;
    }
}
