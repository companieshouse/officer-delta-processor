package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.FormerNames;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.officer.delta.processor.model.PreviousNameArray;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class FormerNameTransformTest {

    private FormerNameTransform testTransform;

    @BeforeEach
    void setUp() {
        testTransform = new FormerNameTransform();
    }

    @Test
    void factory() {
        assertThat(testTransform.factory(), is(instanceOf(FormerNames.class)));
    }

    @Test
    void verifySuccessfulTransformOfFormerNames() {
        final PreviousNameArray previousNameArray = new PreviousNameArray();

        previousNameArray.setPreviousForename("John");
        previousNameArray.setPreviousSurname("Smith");
        previousNameArray.setPreviousTimestamp("20091101072217613702");

        final FormerNames result = testTransform.transform(previousNameArray);

        assertThat(result.getForenames(), is(previousNameArray.getPreviousForename()));
        assertThat(result.getSurname(), is(previousNameArray.getPreviousSurname()));

    }

    @Test
    void testTransformShouldHandleNullSource() {
        assertThat(testTransform.transform((PreviousNameArray) null), is(nullValue()));
    }

}


