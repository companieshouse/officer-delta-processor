package uk.gov.companieshouse.officer.delta.processor.tranformer;

import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Locale;

public class TransformerUtils {

    public static final String TIME_START_OF_DAY = "000000";
    public static final String DATETIME_PATTERN = "yyyyMMddHHmmss";
    public static final String DATE_PATTERN = "yyyyMMdd";
    public static final DateTimeFormatter UTC_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern(DATETIME_PATTERN, Locale.UK).withZone(ZoneId.of("UTC"));
    private static final String SALT = "sdlfksdlkjdf";

    private TransformerUtils() {
        // utility class; prevent instantiation
    }

    public static boolean parseYesOrNo(final String serviceAddressSameAsRegisteredAddress) {
        return "Y".equalsIgnoreCase(serviceAddressSameAsRegisteredAddress);
    }

    /**
     * @param identifier
     * @param s                the string representation of the UTC datetime. Expected to match pattern
     *                         DATETIME_PATTERN.
     * @param effectivePattern the date pattern to mention in the ProcessException
     * @return the Instant corresponding to the parsed string (at UTC by definition)
     * @throws ProcessException if the string cannot be parsed correctly
     */
    private static Instant convertToInstant(final String identifier, final String s, final String effectivePattern)
            throws ProcessException {
        try {
            return Instant.from(UTC_DATETIME_FORMATTER.parse(s));
        }
        catch (DateTimeParseException e) {
            throw new ProcessException(String.format("%s: date/time pattern not matched: [%s]",
                    identifier,
                    effectivePattern), false);
        }
    }

    /**
     * Parse a date string (expected format: DATE_PATTERN).
     * Implementation note: Instant conversion requires input level of detail is Seconds, so TIME_START_OF_DAY is
     * appended before conversion.
     *
     * @param identifier    property name of value (for ProcessException message)
     * @param rawDateString the date string
     * @return the Instant corresponding to the parsed string (at UTC by definition)
     * @throws ProcessException if the string cannot be parsed correctly
     */
    public static Instant parseDateString(final String identifier, String rawDateString) throws ProcessException {
        return convertToInstant(identifier, rawDateString + TIME_START_OF_DAY, DATE_PATTERN);
    }

    /**
     * Parse a date string (expected format: DATETIME_PATTERN).
     *
     * @param identifier        property name of value (for ProcessException message)
     * @param rawDateTimeString the date and time string
     * @return the Instant corresponding to the parsed string (at UTC by definition)
     * @throws ProcessException if the string cannot be parsed correctly
     */
    public static Instant parseDateTimeString(final String identifier, String rawDateTimeString)
            throws ProcessException {
        return convertToInstant(identifier, rawDateTimeString, DATETIME_PATTERN);
    }

    public static String base64Encode(final String plain) {
        return Base64.getUrlEncoder().encodeToString(plain.getBytes(StandardCharsets.UTF_8));
    }

}
