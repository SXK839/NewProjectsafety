
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {
	@Autowired
	MockMvc mvc;
	ObjectMapper mapper = new ObjectMapper();

	@Test
	void add_update_delete_person() throws Exception {
		Person p = new Person("Test", "User", "123 Main St", "Culver", "97451", "000-000-0000", "test@user.com");
		mvc.perform(post("/person").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(p)))
				.andExpect(status().isOk());
		p.setAddress("456 Pine Rd");
		mvc.perform(put("/person").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(p)))
				.andExpect(status().isOk());
		mvc.perform(delete("/person").param("firstName", "Test").param("lastName", "User")).andExpect(status().isOk());
	}
}
