package org.exigencecorp.bindgen.example;

import org.exigencecorp.bindgen.Bindable;

@Bindable
public class PropertyBindingExample {

    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
