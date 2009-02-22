package org.exigencecorp.bindgen.example;

import java.util.ArrayList;
import java.util.List;

import org.exigencecorp.bindgen.Bindable;

@Bindable
public class FieldExample {

    // a read/write property
    public String name;

    // a read/write property with generics
    public List<String> list = new ArrayList<String>();

    public FieldExample(String name) {
        this.name = name;
    }

}
