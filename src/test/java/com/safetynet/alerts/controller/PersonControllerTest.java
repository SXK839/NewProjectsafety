package com.safetynet.alerts.controller;

import com.safetynet.alerts.model.Person;
import com.safetynet.alerts.service.PersonService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonController.class)
class PersonControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PersonService service;

    @Test
    void addPerson_conflict_409() throws Exception {
        String body = "{\"firstName\":\"John\",\"lastName\":\"Boyd\"}";
        when(service.addPerson(any(Person.class))).thenThrow(new IllegalStateException("duplicate"));
        mvc.perform(post("/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isConflict())
           .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void addPerson_badRequest_400() throws Exception {
        String invalid = "{}";
        when(service.addPerson(any(Person.class))).thenThrow(new IllegalArgumentException("missing"));
        mvc.perform(post("/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalid))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void updatePerson_notFound_404() throws Exception {
        when(service.updatePerson(eq("No"), eq("Body"), any(Person.class)))
                .thenThrow(new IllegalArgumentException("Person not found"));
        mvc.perform(put("/person?firstName=No&lastName=Body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"No\",\"lastName\":\"Body\"}"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void updatePerson_badRequest_400_otherIllegalArgument() throws Exception {
        when(service.updatePerson(eq("John"), eq("Boyd"), any(Person.class)))
                .thenThrow(new IllegalArgumentException("Bad data"));
        mvc.perform(put("/person?firstName=John&lastName=Boyd")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Boyd\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void deletePerson_notFound_404() throws Exception {
        when(service.deletePerson("No", "Body")).thenReturn(false);
        mvc.perform(delete("/person?firstName=No&lastName=Body"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void deletePerson_noContent_204() throws Exception {
        when(service.deletePerson("John", "Boyd")).thenReturn(true);
        mvc.perform(delete("/person?firstName=John&lastName=Boyd"))
           .andExpect(status().isNoContent());
    }

    @Test
    void getPerson_notFound_404() throws Exception {
        when(service.getPerson("No", "Body")).thenReturn(java.util.Optional.empty());
        mvc.perform(get("/person?firstName=No&lastName=Body"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getPerson_missingParams_400() throws Exception {
        mvc.perform(get("/person"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").exists());
    }
}