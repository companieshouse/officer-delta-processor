package uk.gov.companieshouse.officer.delta.processor.transformer;

public enum CapitalContributionSubType {
    MONEY("1", "money"),
    LAND_OR_PROPERTY("2", "land-or-property"),
    SHARES("3", "shares"),
    SERVICES_OR_GOODS("4", "services-or-goods"),
    ANY_OTHER_ASSET("5", "any-other-asset");

    private String id;
    private String mappedValue;

    CapitalContributionSubType(String id, String mappedValue) {
        this.id = id;
        this.mappedValue = mappedValue;
    }

    public static String getMappedValue(String id) {
        for (CapitalContributionSubType subType : CapitalContributionSubType.values()) {
            if (subType.id.equals(id)) {
                return subType.mappedValue;
            }
        }

        // Null should be returned if sub-type index does not map to an enum value
        return null;
    }
}
