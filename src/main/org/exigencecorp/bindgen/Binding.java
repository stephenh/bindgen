package org.exigencecorp.bindgen;

public interface Binding<T> {

    String getName();

    Class<?> getType();

    T get();

    void set(T value);

}
