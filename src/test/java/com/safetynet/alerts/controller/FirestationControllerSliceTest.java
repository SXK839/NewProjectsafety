package com.safetynet.alerts.controller;

import com.safetynet.alerts.model.Firestation;
import com.safetynet.alerts.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FirestationController.class)
class FirestationControllerSliceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    AdminService service;

    @Test
    void getByAddress_found_200_and_notFound_404() throws Exception {
        when(service.getFirestationByAddress("1509 Culver St"))
                .thenReturn(Optional.of(new Firestation("1509 Culver St", 3)));
        when(service.getFirestationByAddress("nope"))
                .thenReturn(Optional.empty());

        mvc.perform(get("/firestation").param("address", "1509 Culver St"))
           .andExpect(status().isOk());

        mvc.perform(get("/firestation").param("address", "nope"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getAll_ok_200() throws Exception {
        when(service.getAllFirestations()).thenReturn(List.of(
                new Firestation("A", 1),
                new Firestation("B", 2)
        ));
        mvc.perform(get("/firestation/all"))
           .andExpect(status().isOk());
    }

    @Test
    void deleteByStation_notFound_404_and_noContent_204() throws Exception {
        when(service.deleteFirestationByStation(99)).thenReturn(0);
        when(service.deleteFirestationByStation(3)).thenReturn(2);

        mvc.perform(delete("/firestation").param("station", "99"))
           .andExpect(status().isNotFound());

        mvc.perform(delete("/firestation").param("station", "3"))
           .andExpect(status().isNoContent());
    }
}
