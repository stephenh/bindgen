package org.exigencecorp.bindgen.example;

import org.bindgen.Bindable;

@Bindable
public class SimpleBean {

	private String name;

	public SimpleBean(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
