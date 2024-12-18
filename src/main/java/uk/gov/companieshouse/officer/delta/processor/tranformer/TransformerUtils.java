package uk.gov.companieshouse.officer.delta.processor.tranformer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Locale;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.enums.OfficerRole;

/**
 * The type Transformer utils.
 */
public class TransformerUtils {

    private static final String TIME_START_OF_DAY = "000000";
    private static final String DATETIME_PATTERN = "yyyyMMddHHmmss";
    /**
     * The constant DATETIME_LENGTH.
     */
    public static final int DATETIME_LENGTH = DATETIME_PATTERN.length();
    private static final String MILLISECONDS_DATETIME_PATTERN = "yyyyMMddHHmmssSSS";
    private static final int MILLISECONDS_DATETIME_LENGTH = MILLISECONDS_DATETIME_PATTERN.length();
    private static final String DATE_PATTERN = "yyyyMMdd";
    private static final DateTimeFormatter UTC_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(
            DATETIME_PATTERN, Locale.UK).withZone(ZoneId.of("UTC"));
    private static final DateTimeFormatter MILLISECOND_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern(
            MILLISECONDS_DATETIME_PATTERN);

    private static final String SALT = "ks734s_sdgOc4£b2";

    private TransformerUtils() {
        // utility class; prevent instantiation
    }

    /**
     * Parse yes or no boolean.
     *
     * @param fieldValue the field value
     * @return the boolean
     */
    public static boolean parseYesOrNo(final String fieldValue) {
        return "Y".equalsIgnoreCase(fieldValue);
    }

    /**
     * Parse a date string (expected format: DATE_PATTERN). Implementation note: Instant conversion
     * requires input level of detail is Seconds, so TIME_START_OF_DAY is appended before
     * conversion.
     *
     * @param identifier    property name of value (for Exception message)
     * @param rawDateString the date string
     * @return the LocalDate corresponding to the parsed string (at UTC by definition)
     * @throws NonRetryableErrorException if date parsing fails
     */
    public static LocalDate parseLocalDate(final String identifier, String rawDateString)
            throws NonRetryableErrorException {
        return convertToLocalDate(identifier, rawDateString + TIME_START_OF_DAY, DATE_PATTERN);
    }

    /**
     * Parse a date string (expected format: DATETIME_PATTERN).
     *
     * @param identifier        property name of value (for Exception message)
     * @param rawDateTimeString the date and time string
     * @return the LocalDate corresponding to the parsed string (at UTC by definition)
     * @throws NonRetryableErrorException if datetime parsing fails
     */
    public static LocalDate parseLocalDateTime(final String identifier, String rawDateTimeString)
            throws NonRetryableErrorException {
        final String dateTimeString =
                rawDateTimeString.length() > DATETIME_LENGTH ? rawDateTimeString.substring(0,
                        DATETIME_LENGTH) : rawDateTimeString;

        return convertToLocalDate(identifier, dateTimeString, DATETIME_PATTERN);
    }

    /**
     * Parse a date string (expected format: DATETIME_PATTERN).
     *
     * @param identifier        property name of value (for Exception message)
     * @param rawDateTimeString the date and time string
     * @return the OffsetDateTime corresponding to the parsed string (at UTC by definition)
     * @throws NonRetryableErrorException if datetime parsing fails
     */
    public static OffsetDateTime parseOffsetDateTime(final String identifier,
            String rawDateTimeString) throws NonRetryableErrorException {
        try {
            final String dateTimeString = rawDateTimeString.length() > MILLISECONDS_DATETIME_LENGTH
                    ? rawDateTimeString.substring(0, MILLISECONDS_DATETIME_LENGTH)
                    : rawDateTimeString;

            return LocalDateTime.parse(dateTimeString, MILLISECOND_DATETIME_FORMATTER)
                    .atZone(ZoneId.of("UTC")).toOffsetDateTime();

        } catch (DateTimeParseException ex) {
            throw new NonRetryableErrorException(
                    String.format("%s: date/time pattern not matched: [%s]", identifier,
                            MILLISECONDS_DATETIME_PATTERN), ex);
        }
    }

    /** convertToLocalDate.
     * @param identifier       the field identifier to provide context in error messages
     * @param s                the string representation of the UTC datetime. Expected to match
     *                         pattern DATETIME_PATTERN.
     * @param effectivePattern the date pattern to mention in the Exception
     * @return the LocalDate corresponding to the parsed string (at UTC by definition)
     */
    private static LocalDate convertToLocalDate(final String identifier, final String s,
            final String effectivePattern) throws NonRetryableErrorException {
        try {
            return LocalDate.parse(s, UTC_DATETIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new NonRetryableErrorException(
                    String.format("%s: date/time pattern not matched: [%s]", identifier,
                            effectivePattern), ex);
        }
    }

    /**
     * Base 64 encode string.
     *
     * @param bytes the bytes
     * @return the string
     */
    public static String base64Encode(final byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    /**
     * Sha 1 digest byte [ ].
     *
     * @param plain the plain
     * @return the byte [ ]
     * @throws NonRetryableErrorException the non retryable error exception
     */
    public static byte[] sha1Digest(final String plain) throws NonRetryableErrorException {

        try {
            return MessageDigest.getInstance("SHA-1")
                    .digest((plain + SALT).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new NonRetryableErrorException("Encode failed: no such digest algorithm.", ex);
        }
    }

    /**
     * Encode string.
     *
     * @param plain the plain
     * @return the string
     * @throws NonRetryableErrorException the non retryable error exception
     */
    public static String encode(String plain) throws NonRetryableErrorException {
        return base64Encode(sha1Digest(plain)).replace("=", "");
    }

    /**
     * Lookup officer role string.
     *
     * @param kind    the kind
     * @param corpInd the corp ind
     * @return the string
     */
    public static String lookupOfficerRole(String kind, String corpInd) {
        kind = kind.replace(" ", "");
        if (corpInd != null && !kind.toUpperCase().contains("CORP") && corpInd.equalsIgnoreCase(
                "Y")) {
            kind += "CORP";
        }

        OfficerRole officerRole = OfficerRole.valueOf(kind);

        return officerRole.getValue();
    }
}
