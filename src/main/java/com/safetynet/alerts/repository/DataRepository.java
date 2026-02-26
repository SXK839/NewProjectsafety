package com.safetynet.alerts.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.model.Firestation;
import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.model.Person;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

// ...rest of the class...

@Repository
public class DataRepository {

    private static final Logger log = LoggerFactory.getLogger(DataRepository.class);

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Writable dataset path. Defaults to a filesystem file that we can update.
     * Examples:
     *   file:runtime-data/data.json
     *   file:/var/app/data.json
     */
    @Value("${safetynet.data-file:file:runtime-data/data.json}")
    private Resource dataResource;

    /**
     * Seed dataset to copy when the writable file does not yet exist.
     * Defaults to the classpath data.json bundled with the application.
     */
    @Value("${safetynet.seed-file:classpath:data.json}")
    private Resource seedResource;

    private List<Person> persons = new ArrayList<>();
    private List<Firestation> firestations = new ArrayList<>();
    private List<MedicalRecord> medicalrecords = new ArrayList<>();

    // -------------- Lifecycle --------------

    @PostConstruct
    public void init() {
        try {
            load();
            log.info("Loaded data: persons={}, firestations={}, medicalrecords={}",
                    persons.size(), firestations.size(), medicalrecords.size());

            // Build deterministic distinct station set without method-reference collector
            List<Integer> stationList = firestations.stream()
                    .map(Firestation::getStation)
                    .collect(Collectors.toList());
            Set<Integer> stationSet = new LinkedHashSet<>(stationList);
            log.info("Distinct stations in memory: {}", stationSet);

        } catch (IOException e) {
            log.error("Failed to load dataset", e);
        }
    }

    // -------------- IO --------------

    public synchronized void load() throws IOException {
        File targetFile = resolveWritableDataFile();
        ensureSeedIfMissing(targetFile);

        try (InputStream is = new FileInputStream(targetFile)) {
            Map<String, Object> root = mapper.readValue(is, new TypeReference<Map<String, Object>>() {});
            persons = convertList(root.get("persons"), new TypeReference<List<Person>>() {});
            firestations = convertList(root.get("firestations"), new TypeReference<List<Firestation>>() {});
            medicalrecords = convertList(root.get("medicalrecords"), new TypeReference<List<MedicalRecord>>() {});

            if (persons == null) persons = new ArrayList<>();
            if (firestations == null) firestations = new ArrayList<>();
            if (medicalrecords == null) medicalrecords = new ArrayList<>();
        }
    }

    public synchronized void save() throws IOException {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("persons", persons);
        root.put("firestations", firestations);
        root.put("medicalrecords", medicalrecords);

        File targetFile = resolveWritableDataFile();
        File parent = targetFile.getParentFile();
        if (parent != null) parent.mkdirs();

        mapper.writerWithDefaultPrettyPrinter().writeValue(targetFile, root);
    }

    // -------------- Queries --------------

    public List<Person> getPersons() {
        return persons;
    }

    public List<Firestation> getFirestations() {
        return firestations;
    }

    public List<MedicalRecord> getMedicalrecords() {
        return medicalrecords;
    }

    public Optional<MedicalRecord> findMedical(String f, String l) {
        return medicalrecords.stream()
                .filter(m -> m.getFirstName().equalsIgnoreCase(f)
                        && m.getLastName().equalsIgnoreCase(l))
                .findFirst();
    }

    public Optional<Firestation> findStationByAddress(String a) {
        String key = norm(a);
        return firestations.stream()
                .filter(f -> norm(f.getAddress()).equals(key))
                .findFirst();
    }

    public List<Person> findPersonsByAddress(String a) {
        String key = norm(a);
        List<Person> res = new ArrayList<>();
        for (Person p : persons) {
            if (norm(p.getAddress()).equals(key)) {
                res.add(p);
            }
        }
        return res;
    }

    public List<Person> findPersonsByStation(int s) {
        // Build normalized address set for this station (use two-step to avoid inference issues)
        List<String> addrList = firestations.stream()
                .filter(f -> f.getStation() == s) // primitive int in your model
                .map(Firestation::getAddress)
                .filter(Objects::nonNull)
                .map(this::norm)
                .collect(Collectors.toList());
        Set<String> addrs = new LinkedHashSet<>(addrList);

        // Match normalized person addresses against that set
        List<Person> res = new ArrayList<>();
        for (Person p : persons) {
            String addr = p.getAddress();
            if (addr != null && addrs.contains(norm(addr))) {
                res.add(p);
            }
        }
        return res;
    }

    // -------------- Admin mutations --------------

    public synchronized void addPerson(Person p) throws IOException {
        persons.add(p);
        save();
    }

    public synchronized boolean updatePerson(Person p) throws IOException {
        for (int i = 0; i < persons.size(); i++) {
            Person cur = persons.get(i);
            if (cur.getFirstName().equalsIgnoreCase(p.getFirstName())
                    && cur.getLastName().equalsIgnoreCase(p.getLastName())) {
                // preserve key fields
                p.setFirstName(cur.getFirstName());
                p.setLastName(cur.getLastName());
                persons.set(i, p);
                save();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deletePerson(String f, String l) throws IOException {
        boolean removed = persons.removeIf(x ->
                x.getFirstName().equalsIgnoreCase(f)
                        && x.getLastName().equalsIgnoreCase(l));
        if (removed) save();
        return removed;
    }

    public synchronized void addFirestation(Firestation fm) throws IOException {
        firestations.add(fm);
        save();
    }

    public synchronized boolean updateFirestation(Firestation fm) throws IOException {
        for (int i = 0; i < firestations.size(); i++) {
            Firestation cur = firestations.get(i);
            if (cur.getAddress().equalsIgnoreCase(fm.getAddress())) {
                firestations.set(i, fm);
                save();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteFirestation(String addressOrStation) throws IOException {
        boolean changed;
        try {
            int s = Integer.parseInt(addressOrStation);
            changed = firestations.removeIf(x -> x.getStation() == s);
        } catch (NumberFormatException e) {
            changed = firestations.removeIf(x -> x.getAddress().equalsIgnoreCase(addressOrStation));
        }
        if (changed) save();
        return changed;
    }

    public synchronized int deleteFirestationByStation(int station) throws IOException {
        int before = firestations.size();
        boolean removed = firestations.removeIf(f -> f.getStation() == station);
        int deletedCount = before - firestations.size();
        if (removed) save();
        return deletedCount;
    }

    public synchronized void addMedicalRecord(MedicalRecord m) throws IOException {
        medicalrecords.add(m);
        save();
    }

    public synchronized boolean updateMedicalRecord(MedicalRecord m) throws IOException {
        for (int i = 0; i < medicalrecords.size(); i++) {
            MedicalRecord cur = medicalrecords.get(i);
            if (cur.getFirstName().equalsIgnoreCase(m.getFirstName())
                    && cur.getLastName().equalsIgnoreCase(m.getLastName())) {
                medicalrecords.set(i, m);
                save();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteMedicalRecord(String f, String l) throws IOException {
        boolean removed = medicalrecords.removeIf(x ->
                x.getFirstName().equalsIgnoreCase(f)
                        && x.getLastName().equalsIgnoreCase(l));
        if (removed) save();
        return removed;
    }

    // -------------- Helpers --------------

    private String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private <T> List<T> convertList(Object value, TypeReference<List<T>> type) {
        if (value == null) return new ArrayList<>();
        return mapper.convertValue(value, type);
    }

    /**
     * Resolve a writable data file. If the configured Resource is not a file (e.g., classpath),
     * fall back to "runtime-data/data.json".
     */
    private File resolveWritableDataFile() throws IOException {
        File targetFile;
        try {
            // Works for "file:" resources (and plain file paths)
            targetFile = dataResource.getFile();
        } catch (IOException ex) {
            // Not a file (e.g., classpath resource) or not resolvable -> use runtime fallback
            targetFile = new File("runtime-data/data.json");
        }
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            boolean ok = parent.mkdirs();
            if (!ok) {
                log.warn("Could not create directories for {}", targetFile.getAbsolutePath());
            }
        }
        return targetFile;
    }

    /**
     * If the writable data file does not exist, seed it from the configured seed Resource.
     */
    private void ensureSeedIfMissing(File targetFile) throws IOException {
        if (targetFile.exists()) return;

        log.info("Seeding dataset into {} from {}", targetFile.getAbsolutePath(), seedResource);
        try (InputStream seedIs = seedResource.getInputStream();
             OutputStream os = new FileOutputStream(targetFile)) {
            seedIs.transferTo(os);
        }
    }
}