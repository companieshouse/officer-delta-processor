package uk.gov.companieshouse.officer.delta.processor.transformer;

import java.util.Collection;
import java.util.List;

public interface Transformative<S, T> {
    T transform(S source);

    List<T> transform(Collection<S> sources);
}
