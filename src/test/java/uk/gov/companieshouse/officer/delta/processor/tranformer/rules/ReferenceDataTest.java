package uk.gov.companieshouse.officer.delta.processor.tranformer.rules;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.officer.delta.processor.model.enums.Pre1992Role;

@ExtendWith(MockitoExtension.class)
class ReferenceDataTest {

    @BeforeEach
    void setUp() {

    }

    @ParameterizedTest
    @EnumSource(Pre1992Role.class)
    void isPre1992Role(Pre1992Role role) {

        assertThat(ReferenceData.isPre1992Role(role.toString()), is(true));
    }

    @Test
    void isNonPre1992Role() {

        assertThat(ReferenceData.isPre1992Role("does not match"), is(false));
    }
}