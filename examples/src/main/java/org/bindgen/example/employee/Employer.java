package org.bindgen.example.employee;

import java.util.List;

import org.bindgen.Bindable;

@Bindable
public class Employer {

	public String name;
	public List<Employee> employees;

}
