package uk.gov.companieshouse.officer.delta.processor.model.enums;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyStatusTest {

    @ParameterizedTest
    @MethodSource("companyStatusScenarios")
    void successfullyMapCompanyStatusEnum(CompanyStatusTestArgument argument) {
        // given

        // when
        List<String> companyStatusList = new ArrayList<>();
        for (String statusKey : argument.getDeltaCompanyStatusKeys()) {
            companyStatusList.add(CompanyStatus.statusFromKey(statusKey));
        }

        // then
        assertFalse(companyStatusList.isEmpty());
        assertTrue(companyStatusList.stream().allMatch(
                        companyStatus -> Objects.equals(companyStatus, argument.getExpectedCompanyStatus())));
    }

    private static Stream<Arguments> companyStatusScenarios() {
        return Stream.of(
                Arguments.of(
                        Named.of("Test company status active",
                                CompanyStatusTestArgument.CompanyStatusTestArgumentBuilder()
                                        .withDeltaCompanyStatusList(List.of("0", "5", "Q", "AA", "AB"))
                                        .withCompanyStatus("active")
                                        .build()
                        )
                ),
                Arguments.of(
                        Named.of("Test company status dissolved",
                                CompanyStatusTestArgument.CompanyStatusTestArgumentBuilder()
                                        .withDeltaCompanyStatusList(List.of("1", "R"))
                                        .withCompanyStatus("dissolved")
                                        .build()
                        )
                ),
                Arguments.of(
                        Named.of("Test company status converted-closed",
                                CompanyStatusTestArgument.CompanyStatusTestArgumentBuilder()
                                        .withDeltaCompanyStatusList(List.of("4", "7", "X", "Z"))
                                        .withCompanyStatus("converted-closed")
                                        .build()
                        )
                ),
                Arguments.of(
                        Named.of("Test company status liquidation",
                                CompanyStatusTestArgument.CompanyStatusTestArgumentBuilder()
                                        .withDeltaCompanyStatusList(List.of("2"))
                                        .withCompanyStatus("liquidation")
                                        .build()
                        )
                ),
                Arguments.of(
                        Named.of("Test company status receivership",
                                CompanyStatusTestArgument.CompanyStatusTestArgumentBuilder()
                                        .withDeltaCompanyStatusList(List.of("3", "A", "F", "G"))
                                        .withCompanyStatus("receivership")
                                        .build()
                        )
                ),
                Arguments.of(
                        Named.of("Test company status open",
                                CompanyStatusTestArgument.CompanyStatusTestArgumentBuilder()
                                        .withDeltaCompanyStatusList(List.of("8"))
                                        .withCompanyStatus("open")
                                        .build()
                        )
                ),
                Arguments.of(
                        Named.of("Test company status closed",
                                CompanyStatusTestArgument.CompanyStatusTestArgumentBuilder()
                                        .withDeltaCompanyStatusList(List.of("9"))
                                        .withCompanyStatus("closed")
                                        .build()
                        )
                ),
                Arguments.of(
                        Named.of("Test company status insolvency-proceedings",
                                CompanyStatusTestArgument.CompanyStatusTestArgumentBuilder()
                                        .withDeltaCompanyStatusList(List.of("C", "E", "H", "J", "K", "L", "N", "O", "P", "S", "U", "V", "W"))
                                        .withCompanyStatus("insolvency-proceedings")
                                        .build()
                        )
                ),
                Arguments.of(
                        Named.of("Test company status voluntary_proceedings",
                                CompanyStatusTestArgument.CompanyStatusTestArgumentBuilder()
                                        .withDeltaCompanyStatusList(List.of("I"))
                                        .withCompanyStatus("voluntary_proceedings")
                                        .build()
                        )
                ),
                Arguments.of(
                        Named.of("Test company status administration",
                                CompanyStatusTestArgument.CompanyStatusTestArgumentBuilder()
                                        .withDeltaCompanyStatusList(List.of("M", "T"))
                                        .withCompanyStatus("administration")
                                        .build()
                        )
                ),
                Arguments.of(
                        Named.of("Test company status removed",
                                CompanyStatusTestArgument.CompanyStatusTestArgumentBuilder()
                                        .withDeltaCompanyStatusList(List.of("AD"))
                                        .withCompanyStatus("removed")
                                        .build()
                        )
                ),
                Arguments.of(
                        Named.of("Test company status registered",
                                CompanyStatusTestArgument.CompanyStatusTestArgumentBuilder()
                                        .withDeltaCompanyStatusList(List.of("AC"))
                                        .withCompanyStatus("registered")
                                        .build()
                        )
                )
        );
    }

    private static class CompanyStatusTestArgument {
        private final List<String> deltaCompanyStatusKeys;
        private final String expectedCompanyStatus;

        private CompanyStatusTestArgument(List<String> deltaCompanyStatusKeys, String expectedCompanyStatus) {
            this.deltaCompanyStatusKeys = deltaCompanyStatusKeys;
            this.expectedCompanyStatus = expectedCompanyStatus;
        }


        private static CompanyStatusTestArgumentBuilder CompanyStatusTestArgumentBuilder() {
            return new CompanyStatusTestArgumentBuilder();
        }

        public List<String> getDeltaCompanyStatusKeys() {
            return deltaCompanyStatusKeys;
        }

        public String getExpectedCompanyStatus() {
            return expectedCompanyStatus;
        }

        private static class CompanyStatusTestArgumentBuilder {
            private List<String> deltaCompanyStatusList;
            private String companyStatus;

            public CompanyStatusTestArgumentBuilder withDeltaCompanyStatusList(List<String> statusList) {
                this.deltaCompanyStatusList = statusList;
                return this;
            }

            public CompanyStatusTestArgumentBuilder withCompanyStatus(String status) {
                this.companyStatus = status;
                return this;
            }

            public CompanyStatusTestArgument build() {
                return new CompanyStatusTestArgument(this.deltaCompanyStatusList, this.companyStatus);
            }
        }
    }
}
