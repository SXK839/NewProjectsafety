package com.safetynet.alerts.controller;

import com.safetynet.alerts.dto.PersonInfoDTO;
import com.safetynet.alerts.service.PersonInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonInfoController.class)
class PersonInfoControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PersonInfoService service;

    @Test
    void ok_when_results_found() throws Exception {
        var dto = new PersonInfoDTO();
        dto.firstName = "John"; dto.lastName = "Boyd";
        dto.address = "1509 Culver St"; dto.email = "john@example.com";
        dto.age = 40; dto.medications = List.of(); dto.allergies = List.of();
        when(service.getPersonInfoByLastName("Boyd")).thenReturn(List.of(dto));

        mvc.perform(get("/personInfo").param("lastName","Boyd").accept(APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void not_found_404_when_empty() throws Exception {
        when(service.getPersonInfoByLastName("Nope")).thenReturn(List.of());

        mvc.perform(get("/personInfo").param("lastName","Nope"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void bad_request_400_when_missing_param() throws Exception {
        mvc.perform(get("/personInfo"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}