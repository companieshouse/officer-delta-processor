package uk.gov.companieshouse.officer.delta.processor.tranformer;

import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface Transformative<S, T> {
    // Java uses type erasure for generics. So you cannot create a new instance of a generic class
    // unless you have an instance already, or the class file.
    // This makes the transform default method impossible.
    // This method creates new instances of the output type for that method;
    // Usually this method will simply be: T factory() { return new T(); }
    T factory();

    default T transform(S source) throws ProcessException {
        return transform(source, factory());
    }

    T transform(S source, T output) throws ProcessException;

    default List<T> transform(Collection<S> sources) throws ProcessException {
        List<T> list = new ArrayList<>();
        for (S source : sources) {
            T transform = transform(source);
            list.add(transform);
        }
        return list;
    }
}
