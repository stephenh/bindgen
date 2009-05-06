package org.exigencecorp.bindgen.example.inheritance;

import org.exigencecorp.bindgen.Bindable;
import org.exigencecorp.bindgen.Binding;

import bindgen.org.exigencecorp.bindgen.example.inheritance.BaseExampleBinding;

@Bindable
public class BaseExample {

    public String name;
    public String description;

    public Binding<? extends BaseExample> getBinding() {
        return new BaseExampleBinding(this);
    }

    public void go() {
        this.name = "inbase";
    }
}
