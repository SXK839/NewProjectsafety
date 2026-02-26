package com.safetynet.alerts.service;

import com.safetynet.alerts.model.Firestation;
import com.safetynet.alerts.repository.DataRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminServiceBranchesTest {

    @Test
    void addFirestation_propagates_repo_exceptions_as_expected() throws Exception {
        DataRepository repo = mock(DataRepository.class);
        AdminService svc = new AdminService(repo); // adjust constructor as needed

        // 1) duplicate (conflict)
        doThrow(new IllegalStateException("duplicate"))
                .when(repo).addFirestation(any(Firestation.class));
        assertThatThrownBy(() -> svc.addFirestation(new Firestation("A", 1)))
                .isInstanceOf(IllegalStateException.class);

        // 2) bad request
        doThrow(new IllegalArgumentException("bad"))
                .when(repo).addFirestation(any(Firestation.class));
        assertThatThrownBy(() -> svc.addFirestation(new Firestation("A", 1)))
                .isInstanceOf(IllegalArgumentException.class);

        // 3) IO
        doThrow(new IOException("io fail"))
                .when(repo).addFirestation(any(Firestation.class));
        assertThatThrownBy(() -> svc.addFirestation(new Firestation("A", 1)))
                .isInstanceOfAny(RuntimeException.class, IllegalStateException.class, IllegalArgumentException.class);
    }

    @Test
    void update_delete_getAll_getByAddress_and_deleteByStation_paths() throws Exception {
        DataRepository repo = mock(DataRepository.class);
        AdminService svc = new AdminService(repo);

        when(repo.updateFirestation(any(Firestation.class))).thenReturn(true).thenReturn(false);
        assertThat(svc.updateFirestation(new Firestation("addr", 7))).isTrue();
        assertThat(svc.updateFirestation(new Firestation("addr", 7))).isFalse();

        when(repo.deleteFirestation("addr")).thenReturn(true).thenReturn(false);
        assertThat(svc.deleteFirestation("addr")).isTrue();
        assertThat(svc.deleteFirestation("addr")).isFalse();

        when(repo.deleteFirestationByStation(3)).thenReturn(2).thenReturn(0);
        assertThat(svc.deleteFirestationByStation(3)).isEqualTo(2);
        assertThat(svc.deleteFirestationByStation(3)).isEqualTo(0);

        when(repo.getFirestations()).thenReturn(List.of(new Firestation("A", 1)));
        assertThat(svc.getAllFirestations()).hasSize(1);

        when(repo.findStationByAddress("A")).thenReturn(Optional.of(new Firestation("A", 1)));
        when(repo.findStationByAddress("nope")).thenReturn(Optional.empty());
        assertThat(svc.getFirestationByAddress("A")).isPresent();
        assertThat(svc.getFirestationByAddress("nope")).isEmpty();
    }
}