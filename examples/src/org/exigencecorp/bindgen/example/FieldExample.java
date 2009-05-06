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

    // a read/writer property with wildcards, had been causing errors with ContainerBinding
    public List<?> unknown;

    // a primitive property to show boxing works
    public boolean good;

    // add @Deprecated to show this is skipped--it would generate a warning if the binding got generated
    @Deprecated
    public String[] skipped;

    // we had been colliding on "value"
    public int value;

    // and colliding on "set"
    public boolean set;
    public boolean get;

    // a final field
    public final String finalField = "1";

    public FieldExample(String name) {
        this.name = name;
    }

}
