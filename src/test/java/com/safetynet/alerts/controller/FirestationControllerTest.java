package com.safetynet.alerts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.controller.FirestationController;
import com.safetynet.alerts.model.Firestation;
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

@WebMvcTest(FirestationController.class)
class FirestationControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean AdminService admin;

    @Test
    @DisplayName("POST /firestation -> 201 Created")
    void add_mapping_created() throws Exception {
        var body = new Firestation("1509 Culver St", 3);
        // service.addFirestation(...) is void; no exception means success
        Mockito.doNothing().when(admin).addFirestation(any(Firestation.class));

        mvc.perform(post("/firestation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
           .andExpect(status().isCreated())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.address").value("1509 Culver St"))
           .andExpect(jsonPath("$.station").value(3));
    }

    @Test
    @DisplayName("POST /firestation (duplicate) -> 409 Conflict")
    void add_mapping_conflict() throws Exception {
        var body = new Firestation("1509 Culver St", 3);
        Mockito.doThrow(new IllegalStateException("Mapping already exists for this address"))
               .when(admin).addFirestation(any(Firestation.class));

        mvc.perform(post("/firestation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(body)))
           .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /firestation?address=... -> 200 OK")
    void update_station_ok() throws Exception {
        Mockito.when(admin.updateFirestation(any(Firestation.class))).thenReturn(true);

        mvc.perform(put("/firestation")
                .param("address", "1509 Culver St")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"station\":4}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.address").value("1509 Culver St"))
           .andExpect(jsonPath("$.station").value(4));
    }

    @Test
    @DisplayName("PUT /firestation?address=... (not found) -> 404")
    void update_station_not_found() throws Exception {
        Mockito.when(admin.updateFirestation(any(Firestation.class))).thenReturn(false);

        mvc.perform(put("/firestation")
                .param("address", "unknown")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"station\":4}"))
           .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /firestation?address=... -> 204 No Content")
    void delete_by_address_no_content() throws Exception {
        Mockito.when(admin.deleteFirestation("1509 Culver St")).thenReturn(true);

        mvc.perform(delete("/firestation")
                .param("address", "1509 Culver St"))
           .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /firestation?address=... (not found) -> 404")
    void delete_by_address_not_found() throws Exception {
        Mockito.when(admin.deleteFirestation("unknown")).thenReturn(false);

        mvc.perform(delete("/firestation")
                .param("address", "unknown"))
           .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /firestation?station=3 -> 204 No Content when any deleted")
    void delete_by_station_no_content() throws Exception {
        Mockito.when(admin.deleteFirestationByStation(3)).thenReturn(2); // two mappings deleted

        mvc.perform(delete("/firestation")
                .param("station", "3"))
           .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /firestation?station=3 (none matched) -> 404")
    void delete_by_station_not_found() throws Exception {
        Mockito.when(admin.deleteFirestationByStation(3)).thenReturn(0);

        mvc.perform(delete("/firestation")
                .param("station", "3"))
           .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /firestation?address=... -> 200 OK")
    void get_by_address_ok() throws Exception {
        Mockito.when(admin.getFirestationByAddress("1509 Culver St"))
               .thenReturn(Optional.of(new Firestation("1509 Culver St", 3)));

        mvc.perform(get("/firestation")
                .param("address", "1509 Culver St"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.station").value(3));
    }

    @Test
    @DisplayName("GET /firestation?address=... (not found) -> 404")
    void get_by_address_not_found() throws Exception {
        Mockito.when(admin.getFirestationByAddress("unknown"))
               .thenReturn(Optional.empty());

        mvc.perform(get("/firestation")
                .param("address", "unknown"))
           .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /firestation/all -> 200 OK with array")
    void get_all_ok() throws Exception {
        Mockito.when(admin.getAllFirestations())
               .thenReturn(List.of(
                       new Firestation("1509 Culver St", 3),
                       new Firestation("29 15th St", 2)
               ));

        mvc.perform(get("/firestation/all"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0].address").value("1509 Culver St"))
           .andExpect(jsonPath("$[1].station").value(2));
    }
}