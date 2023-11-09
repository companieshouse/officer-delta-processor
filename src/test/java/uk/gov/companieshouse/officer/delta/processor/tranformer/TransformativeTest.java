package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class TransformativeTest {

    public static final String TZ_PHOENIX = "America/Phoenix";
    public static final String TZ_BAGHDAD = "Asia/Baghdad";
    public static final String TZ_TROLL = "Antarctica/Troll";

    private static class TestTransformative implements Transformative<String, ZoneId> {

        @Override
        public ZoneId factory() {
            return ZoneId.systemDefault();
        }

        @Override
        public ZoneId transform(final String source, final ZoneId output) {
            return ZoneId.of(source);
        }
    }

    private TestTransformative testTransformative;

    @BeforeEach
    void setUp() {
        testTransformative = new TestTransformative();
    }

    @Test
    void factory() {
        assertThat(testTransformative.factory(), is(ZoneId.systemDefault()));
    }

    @Test
    void transformStringZoneId() {
        final ZoneId zoneId = ZoneId.of("Europe/London");

        final ZoneId result = testTransformative.transform(TZ_PHOENIX, zoneId);

        assertThat(result, is(ZoneId.of(TZ_PHOENIX)));
    }

    @Test
    void testTransformStringDefault() throws NonRetryableErrorException {
        assertThat(testTransformative.transform(TZ_PHOENIX), is(ZoneId.of(TZ_PHOENIX)));
    }

    @Test
    void testTransformStringCollection() throws NonRetryableErrorException {
        final List<ZoneId> result = testTransformative.transform(Arrays.asList(TZ_PHOENIX, TZ_BAGHDAD, TZ_TROLL));

        assertThat(result, contains(ZoneId.of(TZ_PHOENIX), ZoneId.of(TZ_BAGHDAD), ZoneId.of(TZ_TROLL)));
    }

    @Test
    void testTransformShouldHandleNullSource() {
        assertThat(testTransformative.transform((String) null), is(nullValue()));
    }
}
