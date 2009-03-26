package org.exigencecorp.bindgen.example.employee;

import org.exigencecorp.bindgen.Bindable;

@Bindable
public class Employee {

    public Employer employer;
    public String name;
    public String department;

    public Employee() {
    }

    public Employee(String name) {
        this.name = name;
    }

}
