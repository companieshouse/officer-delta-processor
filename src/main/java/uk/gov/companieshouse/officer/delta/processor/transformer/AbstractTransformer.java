package uk.gov.companieshouse.officer.delta.processor.transformer;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Transform objects of type S to type T
 *
 * @param <S> The source type
 * @param <T> The target type
 */
public abstract class AbstractTransformer<S, T> implements Transformative<S, T> {
    private final Function<S, T> operation;

    protected AbstractTransformer(final Function<S, T> operation) {
        this.operation = operation;
    }

    @Override
    public final T transform(final S source) {
        return operation.apply(source);
    }

    @Override
    public final List<T> transform(final Collection<S> sources) {
        return sources.stream().map(operation).collect(Collectors.toList());
    }
}
