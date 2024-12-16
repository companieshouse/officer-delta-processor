package uk.gov.companieshouse.officer.delta.processor.tranformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;

/**
 * The interface Transformative.
 *
 * @param <S> the type parameter
 * @param <T> the type parameter
 */
public interface Transformative<S, T> {

    /**
     * Creates a new instance of the output type required by the {@link #transform(Object, Object)}
     * default method.
     * <p>
     * Java uses type erasure for generics. So you cannot create a new instance of a generic class
     * unless you have an instance already, or the class file. This makes the transform default
     * method impossible. This method creates new instances of the output type for that method;
     * </p>
     *
     * <p>Usually the implementation of this method will simply be:
     * <pre>{@code
     * @Override
     * MyTargetClass factory() {
     *   return new MyTargetClass();
     * }
     * }*</pre>
     * <p>for class
     * <pre>{@code
     * class MyTransform implements Transformative<MySourceClass, MyTargetClass>
     * }*
     * </pre>
     * </p>
     *
     * @return new instance of the output type T
     */
    T factory();

    /**
     * Transform t.
     *
     * @param source the source
     * @return the t
     * @throws NonRetryableErrorException the non retryable error exception
     */
    default T transform(S source) throws NonRetryableErrorException {
        if (source == null) {
            return null;
        }
        T target = transform(source, factory());
        if (target.equals(factory())) {
            target = null;
        }
        return target;
    }

    /**
     * Transform t.
     *
     * @param source the source
     * @param output the output
     * @return the t
     * @throws NonRetryableErrorException the non retryable error exception
     */
    T transform(S source, T output) throws NonRetryableErrorException;

    /**
     * Transform list.
     *
     * @param sources the sources
     * @return the list
     * @throws NonRetryableErrorException the non retryable error exception
     */
    default List<T> transform(Collection<S> sources) throws NonRetryableErrorException {
        List<T> list = new ArrayList<>();
        for (S source : sources) {
            T transform = transform(source);
            list.add(transform);
        }
        return list;
    }
}
