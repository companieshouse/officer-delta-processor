package uk.gov.companieshouse.officer.delta.processor.matcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import uk.gov.companieshouse.logging.Logger;

import java.util.Iterator;

/**
 *  Custom matcher class used to match requests made by the consumer to the
 *  data api. The url, request type and request body are compared.
 */
public class OfficerRequestMatcher implements ValueMatcher<Request> {

    private String expectedOutput;
    private String conumb;
    private String id;
    private Logger logger;

    public OfficerRequestMatcher(Logger logger, String conumb, String output, String id) {
        this.conumb = conumb;
        this.expectedOutput = output;
        this.logger = logger;
        this.id = id;
    }

    @Override
    public MatchResult match(Request value) {

        return MatchResult.aggregate(matchUrl(value.getUrl()), matchMethod(value.getMethod()),
                matchBody(value.getBodyAsString()));
    }

    private MatchResult matchUrl(String actualUrl) {
        String expectedUrl = "/company/" + conumb + "/appointments/" + id + "/full_record";

        MatchResult urlResult = MatchResult.of(expectedUrl.equals(actualUrl));

        if (! urlResult.isExactMatch()) {
            logger.error("url does not match expected: <" + expectedUrl + "> actual: <" + actualUrl + ">");
        }

        return urlResult;
    }

    private MatchResult matchMethod(RequestMethod actualMethod) {
        RequestMethod expectedMethod = RequestMethod.PUT;

        MatchResult typeResult = MatchResult.of(expectedMethod.equals(actualMethod));

        if (! typeResult.isExactMatch()) {
            logger.error("Method does not match expected: <" + expectedMethod + "> actual: <" + actualMethod + ">");
        }

        return typeResult;
    }

    private MatchResult matchBody(String actualBody) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode expectedNode = mapper.readTree(expectedOutput);
            JsonNode actualNode = mapper.readTree(actualBody);

            boolean match = compareJson(expectedNode, actualNode);
            if (!match) {
                logger.error("Body does not match expected:\n<" + expectedNode + ">\nactual:\n<" + actualNode + ">");
            }
            return MatchResult.of(match);
        } catch (JsonProcessingException e) {
            logger.error("Could not process JSON: " + e.getMessage());
            return MatchResult.of(false);
        }
    }

    private boolean compareJson(JsonNode expected, JsonNode actual) {
        if (expected == null || actual == null) {
            return expected == actual;
        }

        if (!expected.getNodeType().equals(actual.getNodeType())) {
            return false;
        }

        return switch (expected.getNodeType()) {
            case OBJECT -> compareJsonObjects(expected, actual);
            case ARRAY -> compareJsonArrays(expected, actual);
            default -> expected.equals(actual);
        };
    }

    private boolean compareJsonObjects(JsonNode expected, JsonNode actual) {
        for (Iterator<String> fieldNames = expected.fieldNames(); fieldNames.hasNext(); ) {
            String field = fieldNames.next();
            if (!actual.has(field) || !compareJson(expected.get(field), actual.get(field))) {
                return false;
            }
        }
        return true;
    }

    private boolean compareJsonArrays(JsonNode expected, JsonNode actual) {
        if (expected.size() != actual.size()) {
            return false;
        }
        for (int i = 0; i < expected.size(); i++) {
            if (!compareJson(expected.get(i), actual.get(i))) {
                return false;
            }
        }
        return true;
    }
}

