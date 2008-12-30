package org.exigencecorp.bindgen.example;

import org.exigencecorp.bindgen.Binding;

public class EmployeeBinding implements Binding<Employee> {

    private EmployerBinding employer = null;

    public EmployerBinding employer() {
        if (this.employer == null) {
            this.employer = new MyEmployerBinding();
        }
        return this.employer;
    }

    public class MyEmployerBinding extends EmployerBinding {
        public MyEmployerBinding() {
            super(null);
        }

        @Override
        public String getName() {
            return "employer";
        }

        @Override
        public Employer get() {
            return EmployeeBinding.this.get().employer;
        }

        @Override
        public void set(Employer employer) {
            EmployeeBinding.this.get().employer = employer;
        }
    };

    public StringBinding name = new StringBinding() {
        @Override
        public String getName() {
            return "name";
        }

        @Override
        public String get() {
            return EmployeeBinding.this.get().name;
        }

        @Override
        public void set(String name) {
            EmployeeBinding.this.get().name = name;
        }
    };

    public StringBinding department = new StringBinding() {
        @Override
        public String getName() {
            return "department";
        }

        @Override
        public String get() {
            return EmployeeBinding.this.get().department;
        }

        @Override
        public void set(String department) {
            EmployeeBinding.this.get().department = department;
        }
    };

    private Employee value;

    public EmployeeBinding(Employee value) {
        this.value = value;
    }

    public String getName() {
        return "employee";
    }

    public Class<Employee> getType() {
        return Employee.class;
    }

    public Employee get() {
        return this.value;
    }

    public void set(Employee value) {
        this.value = value;
    }
}
