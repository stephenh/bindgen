package org.exigencecorp.bindgen.example;

import bindgen.java.lang.StringBinding;
import org.exigencecorp.bindgen.Binding;

public class EmployeeBinding implements Binding<Employee> {

    private Employee value;
    private StringBinding department;
    private EmployerBinding employer;
    private StringBinding name;

    public void set(Employee value) {
        this.value = value;
    }

    public Employee get() {
        return this.value;
    }

    public String getName() {
        return "";
    }

    public Class<Employee> getType() {
        return Employee.class;
    }

    public StringBinding department() {
        if (this.department == null) {
            this.department = new MyDepartmentBinding();
        }
        return this.department;
    }

    public EmployerBinding employer() {
        if (this.employer == null) {
            this.employer = new MyEmployerBinding();
        }
        return this.employer;
    }

    public StringBinding name() {
        if (this.name == null) {
            this.name = new MyNameBinding();
        }
        return this.name;
    }

    public class MyDepartmentBinding extends StringBinding {
        public String getName() {
            return "department";
        }
        public String get() {
            return EmployeeBinding.this.get().department;
        }
        public void set(String department) {
            EmployeeBinding.this.get().department = department;
        }
    }

    public class MyEmployerBinding extends EmployerBinding {
        public String getName() {
            return "employer";
        }
        public Employer get() {
            return EmployeeBinding.this.get().employer;
        }
        public void set(Employer employer) {
            EmployeeBinding.this.get().employer = employer;
        }
    }

    public class MyNameBinding extends StringBinding {
        public String getName() {
            return "name";
        }
        public String get() {
            return EmployeeBinding.this.get().name;
        }
        public void set(String name) {
            EmployeeBinding.this.get().name = name;
        }
    }

}
