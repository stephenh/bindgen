package org.exigencecorp.bindgen.example.subpackage;

import org.exigencecorp.bindgen.Bindable;

@Bindable
// Hack for Eclipse not support packages yet
public class PackageExample {

    public String name;

    public PackageExample(String name) {
        this.name = name;
    }

}
