package com.safetynet.alerts.service;

import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.model.Person;
import com.safetynet.alerts.repository.DataRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PersonInfoServiceDateTest {

    @Test
    void with_medical_record_sets_age_and_lists_med_allergies() {
        DataRepository repo = mock(DataRepository.class);

        when(repo.getPersons()).thenReturn(List.of(
                new Person("John","Boyd","1509 Culver St",null,null,null,"john@example.com")
        ));
        when(repo.findMedical("John","Boyd"))
                .thenReturn(Optional.of(new MedicalRecord(
                        "John","Boyd","03/06/1984",
                        List.of("aznol:350mg"), List.of("nillacilan")
                )));

        PersonInfoService svc = new PersonInfoService(repo);
        var out = svc.getPersonInfoByLastName("Boyd");

        assertThat(out).hasSize(1);
        assertThat(out.get(0).age).isGreaterThan(30);
        assertThat(out.get(0).medications).contains("aznol:350mg");
        assertThat(out.get(0).allergies).contains("nillacilan");
    }
}