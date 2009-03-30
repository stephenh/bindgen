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
    public List<Binding<?>> getChildBindings() {
        return new ArrayList<Binding<?>>();
    }

    @Override
    public Binding<?> getParentBinding() {
        return null;
    }

}
