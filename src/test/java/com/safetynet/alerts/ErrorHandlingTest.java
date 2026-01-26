
package com.safetynet.alerts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ErrorHandlingTest {
	@Autowired
	MockMvc mvc;

	@Test
	void bad_station_number_returns_consistent_error_json() throws Exception {
		mvc.perform(get("/firestation").param("stationNumber", "abc")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.path").value("/firestation"));
	}
}
