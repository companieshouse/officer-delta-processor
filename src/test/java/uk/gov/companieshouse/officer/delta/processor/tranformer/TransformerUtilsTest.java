package uk.gov.companieshouse.officer.delta.processor.tranformer;


import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;

class TransformerUtilsTest {

    @ParameterizedTest
    @MethodSource("dateTimeTestArguments")
    void parseOffsetDateTime(DateTimeTestArguments arguments) {
        // when
        OffsetDateTime actual = TransformerUtils.parseOffsetDateTime("id", arguments.getGivenDateTime());

        // then
        assertThat(actual).isEqualTo(arguments.getExpectedDateTime());
    }

    @Test
    void parseOffsetDateTimeInvalidDateTime() {
        // when
        Executable actual = () -> TransformerUtils.parseOffsetDateTime("id", "20200929191703");

        // then
        NonRetryableErrorException exception = assertThrows(NonRetryableErrorException.class, actual);
        assertThat(exception.getMessage()).isEqualTo("id: date/time pattern not matched: [yyyyMMddHHmmssSSS]");
    }

    private static Stream<Arguments> dateTimeTestArguments() {
        return Stream.of(
                Arguments.of(
                        Named.of("Test returns OffsetDateTime when given string contains milliseconds",
                                DateTimeTestArguments.Builder.builder()
                                        .withGivenDateTime("20200929191703971")
                                        .withExpectedDateTime(OffsetDateTime.from(
                                                LocalDateTime.of(
                                                        LocalDate.of(2020, 9, 29),
                                                        LocalTime.of(19, 17, 3, 971000000)).atZone(UTC)))
                                        .build())),
                Arguments.of(
                        Named.of(
                                "Test returns OffsetDateTime shortened to milliseconds when given string contains nanoseconds",
                                DateTimeTestArguments.Builder.builder()
                                        .withGivenDateTime("20200929191703971568")
                                        .withExpectedDateTime(OffsetDateTime.from(
                                                LocalDateTime.of(
                                                        LocalDate.of(2020, 9, 29),
                                                        LocalTime.of(19, 17, 3, 971000000)).atZone(UTC)))
                                        .build())));
    }

    private static class DateTimeTestArguments {

        private final String givenDateTime;
        private final OffsetDateTime expectedDateTime;

        private DateTimeTestArguments(Builder builder) {
            givenDateTime = builder.givenDateTime;
            expectedDateTime = builder.expectedDateTime;
        }

        public String getGivenDateTime() {
            return givenDateTime;
        }

        public OffsetDateTime getExpectedDateTime() {
            return expectedDateTime;
        }

        public static final class Builder {

            private String givenDateTime;
            private OffsetDateTime expectedDateTime;

            private Builder() {
            }

            public static Builder builder() {
                return new Builder();
            }

            public Builder withGivenDateTime(String givenDateTime) {
                this.givenDateTime = givenDateTime;
                return this;
            }

            public Builder withExpectedDateTime(OffsetDateTime expectedDateTime) {
                this.expectedDateTime = expectedDateTime;
                return this;
            }

            public DateTimeTestArguments build() {
                return new DateTimeTestArguments(this);
            }
        }
    }
}