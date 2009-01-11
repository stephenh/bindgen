package org.exigencecorp.bindgen.example;

import bindgen.java.lang.StringBinding;
import org.exigencecorp.bindgen.Binding;

public class MethodExampleBinding implements Binding<MethodExample> {

    private MethodExample value;
    private StringBinding name;
    private StringBinding id;

    public MethodExampleBinding() {
    }

    public MethodExampleBinding(MethodExample value) {
        this.set(value);
    }

    public void set(MethodExample value) {
        this.value = value;
    }

    public MethodExample get() {
        return this.value;
    }

    public String getName() {
        return "";
    }

    public Class<?> getType() {
        return MethodExample.class;
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
            return MethodExampleBinding.this.get().getName();
        }
        public void set(String name) {
            MethodExampleBinding.this.get().setName(name);
        }
    }

    public class MyIdBinding extends StringBinding {
        public String getName() {
            return "id";
        }
        public String get() {
            return MethodExampleBinding.this.get().getId();
        }
        public void set(String id) {
            throw new RuntimeException(this.getName() + " is read only");
        }
    }

}
