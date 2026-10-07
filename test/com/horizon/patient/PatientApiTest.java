package com.horizon.patient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PatientApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @BeforeEach
    void cleanDatabase() {
        patientRepository.deleteAll();
    }

    @Test
    void registersSearchesListsAndUpdatesPatient() throws Exception {
        registerPatient("MRN-1001", "Taylor Morgan", "1990-06-20", "taylor@example.test")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.medicalRecordNumber").value("MRN-1001"));

        mockMvc.perform(get("/api/patients/MRN-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Taylor Morgan"));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(put("/api/patients/MRN-1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Taylor Rivera","dateOfBirth":"1990-06-20",
                                 "email":"taylor.rivera@example.test"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Taylor Rivera"))
                .andExpect(jsonPath("$.email").value("taylor.rivera@example.test"));
    }

    @Test
    void recordsAndListsEncounters() throws Exception {
        registerPatient("MRN-1001", "Taylor Morgan", "1990-06-20", "taylor@example.test")
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/patients/MRN-1001/encounters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2025-01-15","type":"Urgent care",
                                 "clinician":"Dr. Patel","notes":"Follow-up recommended"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("Urgent care"));

        mockMvc.perform(get("/api/patients/MRN-1001/encounters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clinician").value("Dr. Patel"))
                .andExpect(jsonPath("$[0].notes").value("Follow-up recommended"));
    }

    @Test
    void rejectsDuplicateMedicalRecordNumber() throws Exception {
        registerPatient("MRN-1001", "Taylor Morgan", "1990-06-20", "taylor@example.test")
                .andExpect(status().isCreated());

        registerPatient("MRN-1001", "Jordan Lee", "1992-04-10", "jordan@example.test")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A patient with MRN MRN-1001 already exists"));
    }

    @Test
    void rejectsImpossibleAndFutureDates() throws Exception {
        registerPatient("MRN-1001", "Taylor Morgan", "2020-02-30", "taylor@example.test")
                .andExpect(status().isBadRequest());

        registerPatient("MRN-1002", "Future Person", "2999-01-01", "future@example.test")
                .andExpect(status().isBadRequest());
    }

    @Test
    void reportsMissingPatient() throws Exception {
        mockMvc.perform(get("/api/patients/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No patient found with MRN UNKNOWN"));
    }

    private org.springframework.test.web.servlet.ResultActions registerPatient(
            String mrn, String name, String birthDate, String email) throws Exception {
        return mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"medicalRecordNumber":"%s","fullName":"%s",
                         "dateOfBirth":"%s","email":"%s"}
                        """.formatted(mrn, name, birthDate, email)));
    }
}
