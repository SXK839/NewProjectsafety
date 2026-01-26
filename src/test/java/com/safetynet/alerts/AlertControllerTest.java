
package com.safetynet.alerts;

import com.safetynet.alerts.repository.DataRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AlertControllerTest {
	@Autowired
	MockMvc mvc;
	@Autowired
	DataRepository repo;

	@BeforeEach
	void setup() throws Exception {
		repo.load();
	}

	@Test
	void firestation_ok() throws Exception {
		mvc.perform(get("/firestation").param("stationNumber", "1")).andExpect(status().isOk());
	}

	@Test
	void childAlert_empty_when_no_children() throws Exception {
		mvc.perform(get("/childAlert").param("address", "29 15th St")).andExpect(status().isOk())
				.andExpect(jsonPath("$").exists());
	}

	@Test
	void phoneAlert_unknown_station_returns_empty() throws Exception {
		mvc.perform(get("/phoneAlert").param("firestation", "99")).andExpect(status().isOk())
				.andExpect(jsonPath("$").exists());
	}

	@Test
	void fire_known_address_returns_station() throws Exception {
		mvc.perform(get("/fire").param("address", "1509 Culver St")).andExpect(status().isOk())
				.andExpect(jsonPath("$.station").exists());
	}

	@Test
	void floodStations_multiple() throws Exception {
		mvc.perform(get("/flood/stations").param("stations", "1,2")).andExpect(status().isOk());
	}

	@Test
	void personInfo_by_lastname() throws Exception {
		mvc.perform(get("/personInfo").param("lastName", "Boyd")).andExpect(status().isOk());
	}

	@Test
	void communityEmail_city() throws Exception {
		mvc.perform(get("/communityEmail").param("city", "Culver")).andExpect(status().isOk());
	}
}
