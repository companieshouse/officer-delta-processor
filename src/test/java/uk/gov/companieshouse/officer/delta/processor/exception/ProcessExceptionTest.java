package uk.gov.companieshouse.officer.delta.processor.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessExceptionTest {
    final String message = "message";
    @Mock
    Exception cause;

    @Test
    @DisplayName("Fatal exceptions cannot be retried")
    void fatalCantRetry() {
        ProcessException processException = ProcessException.fatal(message, cause);
        assertFalse(processException.canRetry());
    }

    @Test
    @DisplayName("Non-fatal exceptions can be retried")
    void nonFatalCanBeRetried() {
        ProcessException processException = ProcessException.nonFatal(message, cause);
        assertTrue(processException.canRetry());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getterAndSetter(boolean canRetry) {
        ProcessException e = new ProcessException("", null, false);
        ReflectionTestUtils.setField(e, "canRetry", canRetry);

        assertEquals(canRetry, e.canRetry());
    }

}