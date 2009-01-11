package org.exigencecorp.bindgen.example;

import bindgen.java.lang.StringBinding;
import org.exigencecorp.bindgen.Binding;

public class EmployerBinding implements Binding<Employer> {

    private Employer value;
    private StringBinding name;

    public EmployerBinding() {
    }

    public EmployerBinding(Employer value) {
        this.set(value);
    }

    public void set(Employer value) {
        this.value = value;
    }

    public Employer get() {
        return this.value;
    }

    public String getName() {
        return "";
    }

    public Class<?> getType() {
        return Employer.class;
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
            return EmployerBinding.this.get().name;
        }
        public void set(String name) {
            EmployerBinding.this.get().name = name;
        }
    }

}
