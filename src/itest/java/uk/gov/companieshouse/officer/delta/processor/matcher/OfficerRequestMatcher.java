package uk.gov.companieshouse.officer.delta.processor.matcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import uk.gov.companieshouse.logging.Logger;

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

        MatchResult bodyResult;
        JsonNode expectedBody;
        try {
            expectedBody = mapper.readTree(expectedOutput);
        } catch (JsonProcessingException e) {
            logger.error("Could not process expectedBody JSON: " + e);
            return MatchResult.of(false);
        }

        JsonNode actual;
        try {
            actual = mapper.readTree(actualBody);
        } catch (JsonProcessingException e) {
            logger.error("Could not process actualBody JSON: " + e);
            return MatchResult.of(false);
        }

        bodyResult = MatchResult.of(expectedBody.equals(actual));

        if (! bodyResult.isExactMatch()) {
            logger.error("Body does not match expected: <" + expectedBody + "> actual: <" + actualBody + ">");
        }

        return bodyResult;
    }
}

