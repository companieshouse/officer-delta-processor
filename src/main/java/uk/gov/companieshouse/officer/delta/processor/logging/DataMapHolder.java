package uk.gov.companieshouse.officer.delta.processor.logging;

import java.util.Map;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.logging.util.DataMap.Builder;

/**
 * The type Data map holder.
 */
public class DataMapHolder {

    private static final ThreadLocal<DataMap.Builder> DATAMAP_BUILDER = ThreadLocal.withInitial(
            () -> new Builder().requestId("uninitialised"));

    private DataMapHolder() {
    }

    /**
     * Initialise.
     *
     * @param requestId the request id
     */
    public static void initialise(String requestId) {
        DATAMAP_BUILDER.get().requestId(requestId);
    }

    /**
     * Clear.
     */
    public static void clear() {
        DATAMAP_BUILDER.remove();
    }

    /**
     * Get data map . builder.
     *
     * @return the data map . builder
     */
    public static DataMap.Builder get() {
        return DATAMAP_BUILDER.get();
    }

    /**
     * Gets log map.
     *
     * @return the log map
     */
    public static Map<String, Object> getLogMap() {
        return DATAMAP_BUILDER.get().build().getLogMap();
    }

    /**
     * Gets request id.
     *
     * @return the request id
     */
    public static String getRequestId() {
        return (String) getLogMap().get("request_id");
    }
}
