package org.exigencecorp.bindgen.example;

import bindgen.java.lang.StringBinding;
import org.exigencecorp.bindgen.Binding;

public class EmployeeBinding implements Binding<Employee> {

    private Employee value;
    private StringBinding department;
    private EmployerBinding er;
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

    public EmployerBinding er() {
        if (this.er == null) {
            this.er = new MyErBinding();
        }
        return this.er;
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

    public class MyErBinding extends EmployerBinding {
        public String getName() {
            return "er";
        }
        public Employer get() {
            return EmployeeBinding.this.get().er;
        }
        public void set(Employer er) {
            EmployeeBinding.this.get().er = er;
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
