package org.exigencecorp.bindgen.example.inheritance;

import org.exigencecorp.bindgen.Bindable;

import bindgen.org.exigencecorp.bindgen.example.inheritance.SubExampleBinding;

@Bindable
public class SubExample extends BaseExample {

    public String name;
    public String subOnly;

    public SubExampleBinding getBinding() {
        return new SubExampleBinding(this);
    }

}
