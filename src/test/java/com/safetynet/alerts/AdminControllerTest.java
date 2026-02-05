package com.safetynet.alerts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.model.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    MockMvc mvc;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void add_update_delete_person() throws Exception {
        // 1) CREATE (POST) -> 201 Created
        Person p = new Person("Test", "User",
                "123 Main St", "Culver", "97451",
                "000-000-0000", "test@user.com");

        mvc.perform(post("/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(p)))
           .andExpect(status().isCreated())                     // <-- 201 (was isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.firstName").value("Test"))
           .andExpect(jsonPath("$.lastName").value("User"));

        // 2) UPDATE (PUT) -> requires firstName & lastName as query params, returns 200 OK
        //    Only non-name fields need to be sent in the body; names are immutable.
        mvc.perform(put("/person")
                .param("firstName", "Test")
                .param("lastName", "User")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                           "address": "456 Pine Rd",
                           "city": "Culver",
                           "zip": "97451",
                           "phone": "000-000-0000",
                           "email": "test@user.com"
                         }
                         """))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.address").value("456 Pine Rd"))
           .andExpect(jsonPath("$.firstName").value("Test"))
           .andExpect(jsonPath("$.lastName").value("User"));

        // 3) DELETE (DELETE) -> 204 No Content
        mvc.perform(delete("/person")
                .param("firstName", "Test")
                .param("lastName", "User"))
           .andExpect(status().isNoContent());                  // <-- 204 (was isOk())
    }
}
