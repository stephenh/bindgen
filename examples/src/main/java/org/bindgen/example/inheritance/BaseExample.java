package org.bindgen.example.inheritance;

import org.bindgen.Bindable;
import org.bindgen.Binding;

import bindgen.org.bindgen.example.inheritance.BaseExampleBinding;

@Bindable
public class BaseExample {

	public String name;
	public String description;

	public Binding<? extends BaseExample> getBinding() {
		return new BaseExampleBinding(this);
	}

	public void go() {
		this.name = "inbase";
	}
}
