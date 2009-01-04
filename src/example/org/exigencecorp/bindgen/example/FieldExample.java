package org.exigencecorp.bindgen.example;

import org.exigencecorp.bindgen.Bindable;

@Bindable
public class FieldExample {

    // a read/write property
    public String name;

    public FieldExample(String name) {
        this.name = name;
    }

}
