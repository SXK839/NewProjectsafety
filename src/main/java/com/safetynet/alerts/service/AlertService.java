package com.safetynet.alerts.service;

import com.safetynet.alerts.dto.*;
import com.safetynet.alerts.model.*;
import com.safetynet.alerts.repository.DataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business service that implements SafetyNet "query" endpoints
 * (read-only or derived views of the data).
 * <p>
 * This service uses {@link DataRepository} as the in-memory data source and
 * performs aggregation, filtering, and response shaping into DTOs.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Compute coverage by firestation number</li>
 *   <li>List phone numbers for residents covered by a station</li>
 *   <li>Child alert for a given address</li>
 *   <li>Fire endpoint: residents + station + medical info for an address</li>
 *   <li>Flood stations: residents per address for a list of stations</li>
 *   <li>Person info and community email utilities</li>
 * </ul>
 *
 * <p><b>Important:</b> Address comparisons are normalized (trim + lowercase) to
 * be resilient to minor data inconsistencies.</p>
 *
 * @since 1.0
 */
@Service
public class AlertService {
    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final DataRepository repo;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Creates the service with the required data repository.
     *
     * @param repo shared in-memory data repository
     */
    public AlertService(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Converts a birthdate string (MM/dd/yyyy) into an integer age.
     *
     * @param birthdate birthdate string in MM/dd/yyyy format
     * @return computed age in years; {@code -1} if parsing fails
     */
    private int ageFromBirthdate(String birthdate) {
        try {
            LocalDate dob = LocalDate.parse(birthdate, fmt);
            return Period.between(dob, LocalDate.now()).getYears();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Normalizes a string for comparison (trim + lowercase).
     *
     * @param s input string
     * @return normalized string; empty string if {@code null}
     */
    private String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Resolves the set of addresses covered by the given station number.
     * <p>Addresses are normalized.</p>
     *
     * @param stationNumber firestation number
     * @return a set of normalized addresses
     */
    private Set<String> addressesForStation(int stationNumber) {
        return repo.getFirestations().stream()
            .filter(Objects::nonNull)
            .filter(f -> f.getStation() == stationNumber) // primitive int in your model
            .map(Firestation::getAddress)
            .filter(Objects::nonNull)
            .map(this::norm)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Implements <code>GET /firestation?stationNumber=N</code>.
     * <p>
     * Returns a shaped response containing:
     * <ul>
     *   <li>{@code persons}: list of residents (firstName, lastName, address, phone)</li>
     *   <li>{@code adults}: number of adults (age &gt; 18)</li>
     *   <li>{@code children}: number of children (0 ≤ age ≤ 18)</li>
     * </ul>
     * When no residents are covered, the lists are empty and counts are zero.
     * </p>
     *
     * @param stationNumber firestation number
     * @return a map response with keys {@code persons}, {@code adults}, {@code children}
     */
    public Map<String, Object> firestation(int stationNumber) {
        // diagnostics omitted in doc; safe to keep for debugging
        Set<String> addrs = addressesForStation(stationNumber);

        List<Person> persons = repo.getPersons().stream()
            .filter(p -> p.getAddress() != null)
            .filter(p -> addrs.contains(norm(p.getAddress())))
            .toList();

        List<PersonSummaryDTO> list = new ArrayList<>();
        int adults = 0, children = 0;

        for (Person p : persons) {
            PersonSummaryDTO dto = new PersonSummaryDTO();
            dto.firstName = p.getFirstName();
            dto.lastName = p.getLastName();
            dto.address = p.getAddress();
            dto.phone = p.getPhone();
            list.add(dto);

            Optional<MedicalRecord> mr = repo.findMedical(p.getFirstName(), p.getLastName());
            int age = mr.map(m -> ageFromBirthdate(m.getBirthdate())).orElse(-1);
            if (age >= 0 && age <= 18) children++;
            else if (age > 18) adults++;
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("persons", list);
        res.put("adults", adults);
        res.put("children", children);
        return res;
    }

    /**
     * Implements <code>GET /childAlert?address=ADDR</code>.
     * <p>Returns either:
     * <ul>
     *   <li>A list of children at the address, each with age and other household members</li>
     *   <li>Or an empty map if no children are present</li>
     * </ul>
     * </p>
     *
     * @param address address to query
     * @return list of {@link ChildAlertDTO} or an empty map if none
     */
    public Object childAlert(String address) {
        List<Person> ppl = repo.findPersonsByAddress(address);
        List<ChildAlertDTO> children = new ArrayList<>();
        List<String> household = ppl.stream()
            .map(p -> p.getFirstName() + " " + p.getLastName())
            .collect(Collectors.toList());

        for (Person p : ppl) {
            Optional<MedicalRecord> mr = repo.findMedical(p.getFirstName(), p.getLastName());
            int age = mr.map(m -> ageFromBirthdate(m.getBirthdate())).orElse(-1);
            if (age >= 0 && age <= 18) {
                ChildAlertDTO c = new ChildAlertDTO();
                c.firstName = p.getFirstName();
                c.lastName = p.getLastName();
                c.age = age;
                c.otherHouseholdMembers = household.stream()
                    .filter(h -> !h.equals(p.getFirstName() + " " + p.getLastName()))
                    .collect(Collectors.toList());
                children.add(c);
            }
        }
        if (children.isEmpty()) return new HashMap<>();
        return children;
    }

    /**
     * Implements <code>GET /phoneAlert?firestation=N</code>.
     * <p>Returns a map with key {@code phones} containing unique phone numbers
     * for residents covered by the station. Returns an empty list if none.</p>
     *
     * @param stationNumber firestation number
     * @return a map with key {@code phones} and a list value
     */
    public Object phoneAlert(int stationNumber) {
        Set<String> addrs = addressesForStation(stationNumber);

        Set<String> phones = repo.getPersons().stream()
            .filter(p -> p.getAddress() != null)
            .filter(p -> addrs.contains(norm(p.getAddress())))
            .map(Person::getPhone)
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(str -> !str.isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("phones", new ArrayList<>(phones));
        return res;
    }

    /**
     * Implements <code>GET /fire?address=ADDR</code>.
     * <p>Returns station number and resident details (age, phone, meds, allergies)
     * for the given address. When no residents are present, returns an empty map.</p>
     *
     * @param address address to query
     * @return {@link FireResponseDTO} or an empty map if none
     */
    public Object fire(String address) {
        List<Person> ppl = repo.findPersonsByAddress(address);
        if (ppl.isEmpty()) return new HashMap<>();
        int station = repo.findStationByAddress(address).map(Firestation::getStation).orElse(-1);
        List<ResidentDetailsDTO> residents = new ArrayList<>();
        for (Person p : ppl) {
            ResidentDetailsDTO r = new ResidentDetailsDTO();
            r.firstName = p.getFirstName();
            r.lastName = p.getLastName();
            r.phone = p.getPhone();
            Optional<MedicalRecord> mr = repo.findMedical(p.getFirstName(), p.getLastName());
            r.age = mr.map(m -> ageFromBirthdate(m.getBirthdate())).orElse(-1);
            r.medications = mr.map(MedicalRecord::getMedications).orElse(Collections.emptyList());
            r.allergies = mr.map(MedicalRecord::getAllergies).orElse(Collections.emptyList());
            residents.add(r);
        }
        FireResponseDTO dto = new FireResponseDTO();
        dto.station = station;
        dto.residents = residents;
        return dto;
    }

    /**
     * Implements <code>GET /flood/stations?stations=1,2,...</code>.
     * <p>Returns a map of address → residents for all addresses covered by the given stations.</p>
     *
     * @param stations list of station numbers
     * @return map of address to list of {@link ResidentDetailsDTO}; empty map if no addresses
     */
    public Object floodStations(List<Integer> stations) {
        Set<String> addrs = repo.getFirestations().stream()
            .filter(f -> stations.contains(f.getStation()))
            .map(Firestation::getAddress)
            .collect(Collectors.toSet());
        if (addrs.isEmpty()) return new HashMap<>();
        Map<String, List<ResidentDetailsDTO>> out = new LinkedHashMap<>();
        for (String addr : addrs) {
            List<Person> ppl = repo.findPersonsByAddress(addr);
            List<ResidentDetailsDTO> residents = new ArrayList<>();
            for (Person p : ppl) {
                ResidentDetailsDTO r = new ResidentDetailsDTO();
                r.firstName = p.getFirstName();
                r.lastName = p.getLastName();
                r.phone = p.getPhone();
                Optional<MedicalRecord> mr = repo.findMedical(p.getFirstName(), p.getLastName());
                r.age = mr.map(m -> ageFromBirthdate(m.getBirthdate())).orElse(-1);
                r.medications = mr.map(MedicalRecord::getMedications).orElse(Collections.emptyList());
                r.allergies = mr.map(MedicalRecord::getAllergies).orElse(Collections.emptyList());
                residents.add(r);
            }
            out.put(addr, residents);
        }
        return out;
    }

    /**
     * Implements <code>GET /personInfo?lastName=LN</code>.
     * <p>Returns person info DTOs for all persons matching the last name.</p>
     *
     * @param lastName last name to query (case-insensitive)
     * @return list of {@link PersonInfoDTO}; empty list if none
     */
    public Object personInfo(String lastName) {
        List<Person> persons = repo.getPersons().stream()
            .filter(p -> p.getLastName().equalsIgnoreCase(lastName))
            .collect(Collectors.toList());
        if (persons.isEmpty()) return new HashMap<>();
        List<PersonInfoDTO> res = new ArrayList<>();
        for (Person p : persons) {
            Optional<MedicalRecord> mr = repo.findMedical(p.getFirstName(), p.getLastName());
            PersonInfoDTO dto = new PersonInfoDTO();
            dto.firstName = p.getFirstName();
            dto.lastName = p.getLastName();
            dto.address = p.getAddress();
            dto.email = p.getEmail();
            dto.age = mr.map(m -> ageFromBirthdate(m.getBirthdate())).orElse(-1);
            dto.medications = mr.map(MedicalRecord::getMedications).orElse(Collections.emptyList());
            dto.allergies = mr.map(MedicalRecord::getAllergies).orElse(Collections.emptyList());
            res.add(dto);
        }
        return res;
    }

    /**
     * Implements <code>GET /communityEmail?city=CITY</code>.
     * <p>Returns unique emails for all persons living in the given city.</p>
     *
     * @param city city name (case-insensitive)
     * @return a DTO containing the unique emails list or an empty map if none
     */
    public Object communityEmail(String city) {
        List<String> emails = repo.getPersons().stream()
            .filter(p -> p.getCity().equalsIgnoreCase(city))
            .map(Person::getEmail).distinct()
            .collect(Collectors.toList());
        if (emails.isEmpty()) return new HashMap<>();
        CommunityEmailDTO dto = new CommunityEmailDTO();
        dto.emails = emails;
        return dto;
    }
}
