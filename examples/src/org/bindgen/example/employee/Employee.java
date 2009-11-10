package org.bindgen.example.employee;

import org.bindgen.Bindable;

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
