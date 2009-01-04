package org.exigencecorp.bindgen.example;

import org.exigencecorp.bindgen.Bindable;

@Bindable
public class MethodBindingExample {

    // a read-only property
    private String id;
    // a read/write property
    private String name;

    public MethodBindingExample(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
