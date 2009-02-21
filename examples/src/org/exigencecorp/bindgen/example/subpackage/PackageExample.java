package org.exigencecorp.bindgen.example.subpackage;

import org.exigencecorp.bindgen.Bindable;

@Bindable
// Hack until Eclipse 3.5-M5
public class PackageExample {

    public String name;

    public PackageExample(String name) {
        this.name = name;
    }

}
