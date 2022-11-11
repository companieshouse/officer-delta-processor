package uk.gov.companieshouse.officer.delta.processor.tranformer;

import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.enums.OfficerRole;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Locale;

public class TransformerUtils {

    public static final String TIME_START_OF_DAY = "000000";
    public static final String DATETIME_PATTERN = "yyyyMMddHHmmss";
    public static final int DATETIME_LENGTH = DATETIME_PATTERN.length();
    public static final String DATE_PATTERN = "yyyyMMdd";
    public static final DateTimeFormatter UTC_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern(DATETIME_PATTERN, Locale.UK).withZone(ZoneId.of("UTC"));
    private static final String SALT = "ks734s_sdgOc4£b2";

    private TransformerUtils() {
        // utility class; prevent instantiation
    }

    public static boolean parseYesOrNo(final String fieldValue) {
        return "Y".equalsIgnoreCase(fieldValue);
    }

    /**
     * @param identifier       the field identifier to provide context in error messages
     * @param s                the string representation of the UTC datetime. Expected to match pattern
     *                         DATETIME_PATTERN.
     * @param effectivePattern the date pattern to mention in the Exception
     * @return the Instant corresponding to the parsed string (at UTC by definition)
     */
    private static Instant convertToInstant(final String identifier, final String s, final String effectivePattern)
            throws NonRetryableErrorException {
        try {
            return Instant.from(UTC_DATETIME_FORMATTER.parse(s));
        }
        catch (DateTimeParseException e) {
            throw new NonRetryableErrorException(
                    String.format("%s: date/time pattern not matched: [%s]", identifier, effectivePattern), null);
        }
    }

    /**
     * Parse a date string (expected format: DATE_PATTERN).
     * Implementation note: Instant conversion requires input level of detail is Seconds, so TIME_START_OF_DAY is
     * appended before conversion.
     *
     * @param identifier    property name of value (for Exception message)
     * @param rawDateString the date string
     * @return the Instant corresponding to the parsed string (at UTC by definition)
     * @throws NonRetryableErrorException if date parsing fails
     */
    public static Instant parseDateString(final String identifier, String rawDateString)
            throws NonRetryableErrorException {
        return convertToInstant(identifier, rawDateString + TIME_START_OF_DAY, DATE_PATTERN);
    }

    /**
     * Parse a date string (expected format: DATETIME_PATTERN).
     *
     * @param identifier        property name of value (for sException message)
     * @param rawDateTimeString the date and time string
     * @return the Instant corresponding to the parsed string (at UTC by definition)
     * @throws NonRetryableErrorException if datetime parsing fails
     */
    public static Instant parseDateTimeString(final String identifier, String rawDateTimeString)
            throws NonRetryableErrorException {
        final String dateTimeString = rawDateTimeString.length() > DATETIME_LENGTH
                ? rawDateTimeString.substring(0, DATETIME_LENGTH)
                : rawDateTimeString;

        return convertToInstant(identifier, dateTimeString, DATETIME_PATTERN);
    }

    public static String base64Encode(final byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    public static byte[] sha1Digest(final String plain) throws NonRetryableErrorException {

        try {
            return MessageDigest.getInstance("SHA-1").digest((plain + SALT).getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException e) {
            throw new NonRetryableErrorException("Encode failed: no such digest algorithm.", e);
        }

    }

    public static String encode(String plain) throws NonRetryableErrorException {
        return base64Encode(sha1Digest(plain)).replace("=", "");
    }

    public static String lookupOfficerRole(String kind, String corpInd) {
        kind = kind.replace(" ","");
        if(corpInd != null && !kind.toUpperCase().contains("CORP")
                && corpInd.equalsIgnoreCase("Y")) {
            kind += "CORP";
        }

        OfficerRole officerRole = OfficerRole.valueOf(kind);

        return officerRole.getValue();
    }
}
