package org.exigencecorp.bindgen;

import java.util.List;

/**
 * Interface for a property (field or method) binding.
 */
public interface Binding<T> extends NamedBinding {

    List<Binding<?>> getBindings();

    Class<?> getType();

    T get();

    void set(T value);

}
