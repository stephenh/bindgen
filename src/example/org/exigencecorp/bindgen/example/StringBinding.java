package org.exigencecorp.bindgen.example;

import org.exigencecorp.bindgen.Binding;

public class StringBinding implements Binding<String> {

    private String value;

    public String getName() {
        return "string";
    }

    public Class<String> getType() {
        return String.class;
    }

    public String get() {
        return this.value;
    }

    public void set(String value) {
        this.value = value;
    }
}
