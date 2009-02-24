package org.exigencecorp.bindgen;

import java.util.List;

public interface Binding<T> {

    String getName();

    List<Binding<?>> getBindings();

    Class<?> getType();

    T get();

    void set(T value);

}
