package org.exigencecorp.bindgen.example;

import org.exigencecorp.bindgen.Bindable;

@Bindable
public class GenericsExample<T> {

    private T foo;
    public T bar;

    public T getFoo() {
        return this.foo;
    }

    public void setFoo(T foo) {
        this.foo = foo;
    }

}
