package com.safetynet.alerts.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** IMPORTANT: The annotation must be on the class, not inside the body */
@WebMvcTest(MedicalRecordController.class)
class MedicalRecordControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean AdminService admin;

    private MedicalRecord sample() {
        MedicalRecord m = new MedicalRecord();
        m.setFirstName("John1");
        m.setLastName("Doe1");
        m.setBirthdate("03/06/1984");
        m.setMedications(List.of("aznol:350mg"));
        m.setAllergies(List.of("nillacilan"));
        return m;
    }

    @Test
    @DisplayName("POST /medicalRecord -> 201 Created")
    void add_medical_record_created() throws Exception {
        var m = sample();
        Mockito.when(admin.getMedicalRecord("John1","Doe1")).thenReturn(Optional.empty());
        Mockito.doNothing().when(admin).addMedicalRecord(any(MedicalRecord.class));

        mvc.perform(post("/medicalRecord")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(m)))
           .andExpect(status().isCreated())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.firstName").value("John1"))
           .andExpect(jsonPath("$.lastName").value("Doe1"));
    }

    @Test
    @DisplayName("POST /medicalRecord (duplicate) -> 409 Conflict")
    void add_medical_record_conflict() throws Exception {
        var m = sample();
        Mockito.when(admin.getMedicalRecord("John1","Doe1"))
               .thenReturn(Optional.of(m)); // exists

        mvc.perform(post("/medicalRecord")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(m)))
           .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /medicalRecord?firstName&lastName -> 200 OK")
    void update_medical_record_ok() throws Exception {
        Mockito.when(admin.updateMedicalRecord(any(MedicalRecord.class))).thenReturn(true);

        mvc.perform(put("/medicalRecord")
                .param("firstName", "John1")
                .param("lastName", "Doe1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                           "birthdate":"03/06/1984",
                           "medications":["aznol:100mg"],
                           "allergies":["nillacilan","peanut"]
                         }
                         """))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.firstName").value("John1"))
           .andExpect(jsonPath("$.lastName").value("Doe1"))
           .andExpect(jsonPath("$.medications[0]").value("aznol:100mg"));
    }

    @Test
    @DisplayName("PUT /medicalRecord?firstName&lastName (not found) -> 404")
    void update_medical_record_not_found() throws Exception {
        Mockito.when(admin.updateMedicalRecord(any(MedicalRecord.class))).thenReturn(false);

        mvc.perform(put("/medicalRecord")
                .param("firstName", "John1")
                .param("lastName", "Doe1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                           "birthdate":"03/06/1984",
                           "medications":["aznol:100mg"],
                           "allergies":["peanut"]
                         }
                         """))
           .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /medicalRecord?firstName&lastName -> 204 No Content")
    void delete_medical_record_no_content() throws Exception {
        Mockito.when(admin.deleteMedicalRecord("John1","Doe1")).thenReturn(true);

        mvc.perform(delete("/medicalRecord")
                .param("firstName", "John1")
                .param("lastName", "Doe1"))
           .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /medicalRecord?firstName&lastName (not found) -> 404")
    void delete_medical_record_not_found() throws Exception {
        Mockito.when(admin.deleteMedicalRecord("John1","Doe1")).thenReturn(false);

        mvc.perform(delete("/medicalRecord")
                .param("firstName", "John1")
                .param("lastName", "Doe1"))
           .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /medicalRecord?firstName&lastName -> 200 OK")
    void get_one_ok() throws Exception {
        Mockito.when(admin.getMedicalRecord("John1","Doe1"))
               .thenReturn(Optional.of(sample()));

        mvc.perform(get("/medicalRecord")
                .param("firstName", "John1")
                .param("lastName", "Doe1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.firstName").value("John1"))
           .andExpect(jsonPath("$.allergies[0]").value("nillacilan"));
    }

    @Test
    @DisplayName("GET /medicalRecord?firstName&lastName (not found) -> 404")
    void get_one_not_found() throws Exception {
        Mockito.when(admin.getMedicalRecord("John1","Doe1"))
               .thenReturn(Optional.empty());

        mvc.perform(get("/medicalRecord")
                .param("firstName", "John1")
                .param("lastName", "Doe1"))
           .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /medicalRecord/all -> 200 OK with array")
    void get_all_ok() throws Exception {
        Mockito.when(admin.getAllMedicalRecords())
               .thenReturn(List.of(sample()));

        mvc.perform(get("/medicalRecord/all"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0].firstName").value("John1"));
    }
}