package org.exigencecorp.bindgen.example.employee;

import java.util.List;

import org.exigencecorp.bindgen.Bindable;

@Bindable
public class Employer {

    public String name;
    public List<Employee> employees;

}
