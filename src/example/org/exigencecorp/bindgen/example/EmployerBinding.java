package org.exigencecorp.bindgen.example;

import org.exigencecorp.bindgen.Binding;

public class EmployerBinding implements Binding<Employer> {

    public StringBinding name = new StringBinding() {
        @Override
        public String getName() {
            return "name";
        }

        @Override
        public String get() {
            return EmployerBinding.this.get().name;
        }

        @Override
        public void set(String name) {
            EmployerBinding.this.get().name = name;
        }
    };

    private Employer value;

    public EmployerBinding(Employer e) {
        this.value = e;
    }

    public String getName() {
        return "employer";
    }

    public Class<Employer> getType() {
        return Employer.class;
    }

    public Employer get() {
        return this.value;
    }

    public void set(Employer value) {
        this.value = value;
    }

}
