package org.exigencecorp.bindgen.example;

import bindgen.java.lang.StringBinding;
import org.exigencecorp.bindgen.Binding;

public class MethodBindingExampleBinding implements Binding<MethodBindingExample> {

    private MethodBindingExample value;
    private StringBinding name;
    private StringBinding id;

    public MethodBindingExampleBinding() {
    }

    public MethodBindingExampleBinding(MethodBindingExample value) {
        this.set(value);
    }

    public void set(MethodBindingExample value) {
        this.value = value;
    }

    public MethodBindingExample get() {
        return this.value;
    }

    public String getName() {
        return "";
    }

    public Class<MethodBindingExample> getType() {
        return MethodBindingExample.class;
    }

    public StringBinding name() {
        if (this.name == null) {
            this.name = new MyNameBinding();
        }
        return this.name;
    }

    public StringBinding id() {
        if (this.id == null) {
            this.id = new MyIdBinding();
        }
        return this.id;
    }

    public class MyNameBinding extends StringBinding {
        public String getName() {
            return "name";
        }
        public String get() {
            return MethodBindingExampleBinding.this.get().getName();
        }
        public void set(String name) {
            MethodBindingExampleBinding.this.get().setName(name);
        }
    }

    public class MyIdBinding extends StringBinding {
        public String getName() {
            return "id";
        }
        public String get() {
            return MethodBindingExampleBinding.this.get().getId();
        }
        public void set(String id) {
            throw new RuntimeException(this.getName() + " is read only");
        }
    }

}
