package com.safetynet.alerts.repository;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.model.Firestation;
import com.safetynet.alerts.model.Firestation;
import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.model.Person;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataRepositoryTest {

 private DataRepository repo;
 private File tempDir;
 private File dataFile;

 @BeforeEach
 void setUp() throws Exception {
     tempDir = Files.createTempDirectory("safetynet-test").toFile();
     dataFile = new File(tempDir, "data.json");

     // Copy a small seed JSON to the temp data.json
     var seed = new ClassPathResource("test-seed.json");
     Files.copy(seed.getInputStream(), dataFile.toPath());

     repo = new DataRepository();

     // Wire @Value fields via reflection: data-file -> temp file, seed-file -> classpath seed
     setField(repo, "dataResource", new FileSystemResource(dataFile));
     setField(repo, "seedResource", new ClassPathResource("test-seed.json"));

     // Load collections
     repo.load();
 }

 @AfterEach
 void tearDown() {
     // Best-effort cleanup of temp dir
     if (tempDir != null && tempDir.exists()) {
         deleteRecursively(tempDir);
     }
 }

 private static void setField(Object target, String fieldName, Object value) throws Exception {
     var f = DataRepository.class.getDeclaredField(fieldName);
     f.setAccessible(true);
     f.set(target, value);
 }

 private static void deleteRecursively(File f) {
     if (f.isDirectory()) {
         File[] children = f.listFiles();
         if (children != null) {
             for (File c : children) deleteRecursively(c);
         }
     }
     //noinspection ResultOfMethodCallIgnored
     f.delete();
 }

 // ---------- Basic load / getters ----------

 @Test
 void load_shouldPopulateCollections() {
     assertThat(repo.getPersons()).hasSize(2);
     assertThat(repo.getFirestations()).hasSize(2);
     assertThat(repo.getMedicalrecords()).hasSize(1);
 }

 @Test
 void save_shouldWriteToDataFile() throws Exception {
     repo.addPerson(new Person("New", "Person", "Somewhere", "City", "00000", "000-000", "new@example.com"));
     repo.save();

     var mapper = new ObjectMapper();
     var root = mapper.readTree(dataFile);
     assertThat(root.get("persons")).hasSize(3);
 }

 // ---------- Person: add / update (found/not) / delete (found/not) ----------

 @Test
 void add_update_delete_person_branches() throws Exception {
     var p = new Person("Alice", "Doe", "Addr", "City", "Zip", "123", "a@b.com");

     repo.addPerson(p);
     assertThat(repo.getPersons())
             .anyMatch(x -> x.getFirstName().equals("Alice") && x.getLastName().equals("Doe"));

     // Update found: same first/last, new fields
     var updated = new Person("Alice", "Doe", "NewAddr", "NewCity", "99999", "999", "new@b.com");
     assertThat(repo.updatePerson(updated)).isTrue();
     assertThat(repo.getPersons())
             .anyMatch(x -> x.getFirstName().equals("Alice")
                          && x.getLastName().equals("Doe")
                          && "NewAddr".equals(x.getAddress()));

     // Update not found
     var notFound = new Person("Alice", "X", "Y", null, null, null, null);
     assertThat(repo.updatePerson(notFound)).isFalse();

     // Delete found
     assertThat(repo.deletePerson("Alice", "Doe")).isTrue();
     assertThat(repo.getPersons())
             .noneMatch(x -> x.getFirstName().equals("Alice") && x.getLastName().equals("Doe"));

     // Delete not found
     assertThat(repo.deletePerson("No", "Body")).isFalse();
 }

 // ---------- Firestation: update (found/not) / delete by station (numeric) or address (non-numeric) ----------

 @Test
 void update_firestation_found_and_not_found() throws Exception {
     // Found by address
     var fmFound = new Firestation("1509 Culver St", 4);
     assertThat(repo.updateFirestation(fmFound)).isTrue();
     assertThat(repo.getFirestations())
             .anyMatch(f -> f.getAddress().equalsIgnoreCase("1509 Culver St") && f.getStation() == 4);

     // Not found
     var fmNotFound = new Firestation("Unknown Address", 9);
     assertThat(repo.updateFirestation(fmNotFound)).isFalse();
 }

 @Test
 void delete_firestation_by_stationNumber_and_by_address() throws Exception {
     // numeric branch
     assertThat(repo.deleteFirestation("3")).isTrue();     // seed has station 3
     // non-numeric branch
     assertThat(repo.deleteFirestation("29 15th St")).isTrue();
     // nothing to delete
     assertThat(repo.deleteFirestation("999")).isFalse();
     assertThat(repo.deleteFirestation("No Such Address")).isFalse();
 }

 @Test
 void delete_firestation_by_station_count_returns_deleted_number() throws Exception {
     // Re-load fresh to ensure station 3 exists
     repo.load();
     int deleted = repo.deleteFirestationByStation(3);
     assertThat(deleted).isGreaterThanOrEqualTo(1);

     // Deleting again should return 0
     assertThat(repo.deleteFirestationByStation(3)).isEqualTo(0);
 }

 // ---------- MedicalRecord: add / update (found/not) / delete (found/not) ----------

 @Test
 void add_update_delete_medicalRecord_branches() throws Exception {
     var m = new MedicalRecord("Alice", "Doe", "01/01/2000", List.of("med:10"), List.of());
     repo.addMedicalRecord(m);
     assertThat(repo.findMedical("Alice", "Doe")).isPresent();

     // update found
     var m2 = new MedicalRecord("Alice", "Doe", "01/01/2001", List.of(), List.of("pollen"));
     assertThat(repo.updateMedicalRecord(m2)).isTrue();
     assertThat(repo.findMedical("Alice", "Doe").get().getBirthdate()).isEqualTo("01/01/2001");

     // update not found
     var m3 = new MedicalRecord("Ghost", "User", "01/01/2010", List.of(), List.of());
     assertThat(repo.updateMedicalRecord(m3)).isFalse();

     // delete found
     assertThat(repo.deleteMedicalRecord("Alice", "Doe")).isTrue();
     // delete not found
     assertThat(repo.deleteMedicalRecord("No", "Body")).isFalse();
 }

 // ---------- Query helpers ----------

 @Test
 void find_helpers_cover_branches() {
     assertThat(repo.findMedical("John", "Boyd")).isPresent();
     assertThat(repo.findMedical("john", "boyd")).isPresent(); // case-insensitive

     assertThat(repo.findStationByAddress("1509 Culver St")).isPresent();
     assertThat(repo.findStationByAddress("none")).isEmpty();

     assertThat(repo.findPersonsByAddress("1509 Culver St")).hasSizeGreaterThanOrEqualTo(1);
     assertThat(repo.findPersonsByAddress("nope")).isEmpty();

     assertThat(repo.findPersonsByStation(3)).hasSizeGreaterThanOrEqualTo(1);
     assertThat(repo.findPersonsByStation(999)).isEmpty();
 }

 @Test
 void load_again_is_idempotent() throws Exception {
     // calling load second time should not crash and should keep data consistent
     repo.load();
     assertThat(repo.getPersons()).isNotEmpty();
     assertThat(repo.getFirestations()).isNotEmpty();
     assertThat(repo.getMedicalrecords()).isNotEmpty();
 }
}