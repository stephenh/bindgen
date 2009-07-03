package org.exigencecorp.bindgen;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBinding<T> implements Binding<T> {

    protected T _value;

    @Override
    public T get() {
        return this._value;
    }

    @Override
    public void set(T value) {
        this._value = value;
    }

    @Override
    public T getWithRoot(Object root) {
        return (T) root;
    }

    @Override
    public void setWithRoot(Object root, T value) {
        throw new RuntimeException("Should be overridden by a field/method-specific binding.");
    }

    @Override
    public List<Binding<?>> getChildBindings() {
        return new ArrayList<Binding<?>>();
    }

    @Override
    public Binding<?> getParentBinding() {
        return null;
    }

}
