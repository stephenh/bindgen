package org.exigencecorp.bindgen.example.inheritance;

import org.exigencecorp.bindgen.Bindable;

import bindgen.org.exigencecorp.bindgen.example.inheritance.BaseExampleBinding;

@Bindable
public class BaseExample {

    public String name;
    public String description;

    public BaseExampleBinding getBinding() {
        return new BaseExampleBinding(this);
    }

}
