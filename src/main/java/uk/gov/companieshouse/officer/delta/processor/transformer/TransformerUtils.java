package uk.gov.companieshouse.officer.delta.processor.transformer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

public class TransformerUtils {

    public static boolean parseYesOrNo(final String serviceAddressSameAsRegisteredAddress) {
        return serviceAddressSameAsRegisteredAddress.equalsIgnoreCase("Y");
    }

    public static LocalDateTime parseBackwardsDate(String rawBackwardsDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.UK);
        return LocalDate.parse(rawBackwardsDate, formatter).atStartOfDay();
        // TODO: should this be an Instant (i.e. UTC) or a LocalDate?
        // mongodb driver 3.7 has JSR-310 Instant, LocalDate & LocalDateTime support
        // Check saved values in mongodb are not adjusted for DST
    }

    static String base64Encode(final String plain) {
        return Base64.getUrlEncoder().encodeToString(plain.getBytes(StandardCharsets.UTF_8));
    }
}
