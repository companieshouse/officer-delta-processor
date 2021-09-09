package uk.gov.companieshouse.officer.delta.processor.tranformer;

import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

public class TransformerUtils {

    private static final String SALT = "ks734s_sdgOc4£b2";

    public static boolean parseYesOrNo(final String serviceAddressSameAsRegisteredAddress) {
        return serviceAddressSameAsRegisteredAddress.equalsIgnoreCase("Y");
    }

    public static LocalDateTime parseBackwardsDate(String rawBackwardsDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.UK);
        return LocalDate.parse(rawBackwardsDate, formatter).atStartOfDay();
    }

    public static String base64Encode(final byte[] bytes) {

        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    public static byte[] sha1Digest(final String plain) throws ProcessException {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new ProcessException("Encode failed.", e, false);
        }

        return md.digest((plain + SALT).getBytes(StandardCharsets.UTF_8));
    }
}
