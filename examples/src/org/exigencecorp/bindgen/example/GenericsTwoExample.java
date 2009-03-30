package org.exigencecorp.bindgen.example;

import java.util.List;

import org.exigencecorp.bindgen.Bindable;

@Bindable
public class GenericsTwoExample<T extends List<?>> {

    private T foo;
    public T bar;

    public T getFoo() {
        return this.foo;
    }

    public void setFoo(T foo) {
        this.foo = foo;
    }

}
