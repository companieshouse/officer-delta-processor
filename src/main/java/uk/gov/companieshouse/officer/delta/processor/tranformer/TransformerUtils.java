package uk.gov.companieshouse.officer.delta.processor.tranformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TransformerUtils {

    public static boolean parseYesOrNo(final String serviceAddressSameAsRegisteredAddress) {
        return serviceAddressSameAsRegisteredAddress.equalsIgnoreCase("Y");
    }

    public static LocalDateTime parseBackwardsDate(String rawBackwardsDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.UK);
        return LocalDate.parse(rawBackwardsDate, formatter).atStartOfDay();
    }
}
