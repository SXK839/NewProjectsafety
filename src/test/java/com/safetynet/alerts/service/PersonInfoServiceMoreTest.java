package com.safetynet.alerts.service;

import com.safetynet.alerts.model.Person;
import com.safetynet.alerts.repository.DataRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PersonInfoServiceMoreTest {

    @Test
    void returns_empty_when_no_lastName_matches() {
        DataRepository repo = mock(DataRepository.class);
        when(repo.getPersons()).thenReturn(List.of(
                new Person("A","X","addr",null,null,null,"a@x.com")
        ));
        PersonInfoService svc = new PersonInfoService(repo);
        var out = svc.getPersonInfoByLastName("Boyd");
        assertThat(out).isEmpty();
    }

    @Test
    void case_insensitive_lastName_and_missing_medical_record_sets_age_minus1() {
        DataRepository repo = mock(DataRepository.class);
        when(repo.getPersons()).thenReturn(List.of(
                new Person("Emily","Boyd","addr",null,null,null,"e@b.com")
        ));
        when(repo.findMedical("Emily","Boyd")).thenReturn(java.util.Optional.empty());

        PersonInfoService svc = new PersonInfoService(repo);
        var out = svc.getPersonInfoByLastName("bOyD");
        assertThat(out).hasSize(1);
        assertThat(out.get(0).age).isEqualTo(-1);
    }
}