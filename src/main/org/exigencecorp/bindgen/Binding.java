package org.exigencecorp.bindgen;

public interface Binding<T> {

    String getName();

    Class<T> getType();

    T get();

    void set(T value);

}
