package org.exigencecorp.bindgen.example.inheritance;

import bindgen.org.exigencecorp.bindgen.example.inheritance.SubExampleBinding;

public class SubExample extends BaseExample {

    public String name;
    public String subOnly;

    public SubExampleBinding getBinding() {
        return new SubExampleBinding(this);
    }

    @Override
    public void go() {
        this.name = "insub";
    }

}
