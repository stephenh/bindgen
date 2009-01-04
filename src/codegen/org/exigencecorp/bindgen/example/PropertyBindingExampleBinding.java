package org.exigencecorp.bindgen.example;

import bindgen.java.lang.StringBinding;
import org.exigencecorp.bindgen.Binding;

public class PropertyBindingExampleBinding implements Binding<PropertyBindingExample> {

    private PropertyBindingExample value;
    private StringBinding name;

    public void set(PropertyBindingExample value) {
        this.value = value;
    }

    public PropertyBindingExample get() {
        return this.value;
    }

    public String getName() {
        return "";
    }

    public Class<PropertyBindingExample> getType() {
        return PropertyBindingExample.class;
    }

    public StringBinding name() {
        if (this.name == null) {
            this.name = new MyNameBinding();
        }
        return this.name;
    }

    public class MyNameBinding extends StringBinding {
        public String getName() {
            return "name";
        }
        public String get() {
            return PropertyBindingExampleBinding.this.get().getName();
        }
        public void set(String name) {
            PropertyBindingExampleBinding.this.get().setName(name);
        }
    }

}
