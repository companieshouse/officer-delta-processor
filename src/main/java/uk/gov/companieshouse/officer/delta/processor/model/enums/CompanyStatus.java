package uk.gov.companieshouse.officer.delta.processor.model.enums;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum CompanyStatus {

    ACTIVE(List.of("0", "5", "Q", "AA", "AB"), "active"),
    DISSOLVED(List.of("1", "R"), "dissolved"),
    CONVERTED_CLOSED(List.of("4", "7", "X", "Z"), "converted-closed"),
    LIQUIDATION(List.of("2"), "liquidation"),
    RECEIVERSHIP(List.of("3", "A", "F", "G"), "receivership"),
    OPEN(List.of("8"), "open"),
    CLOSED(List.of("9"), "closed"),
    INSOLVENCY_PROCEEDINGS(List.of("C", "E", "H", "J", "K", "L", "N", "O", "P", "S", "U", "V", "W"), "insolvency-proceedings"),
    VOLUNTARY_PROCEEDINGS(List.of("I"), "voluntary_proceedings"),
    ADMINISTRATION(List.of("M", "T"), "administration"),
    REMOVED(List.of("AD"), "removed"),
    REGISTERED(List.of("AC"), "registered");

    private static final Map<String, String> STATUS_MAP = new HashMap<>();

    static {
        for (CompanyStatus companyStatus: values()) {
            companyStatus.getKeys().forEach(key -> STATUS_MAP.put(key, companyStatus.getStatus()));
        }
    }

    private final List<String> keys;
    private final String status;

    CompanyStatus(List<String> keys, String status) {
        this.keys = keys;
        this.status = status;
    }

    public List<String> getKeys() {
        return keys;
    }

    public String getStatus() {
        return status;
    }

    public static String statusFromKey(String value) {
        return STATUS_MAP.get(value);
    }
}