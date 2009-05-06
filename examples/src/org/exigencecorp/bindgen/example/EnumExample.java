package org.exigencecorp.bindgen.example;

import org.exigencecorp.bindgen.Bindable;

@Bindable
public class EnumExample {

    public Foo foo;

    public enum Foo {
        ONE, TWO
    }

}
