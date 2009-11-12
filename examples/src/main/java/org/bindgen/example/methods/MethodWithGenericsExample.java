package org.bindgen.example.methods;

import java.util.ArrayList;
import java.util.List;

import org.bindgen.Bindable;

@Bindable
public class MethodWithGenericsExample {

	private List<String> list = new ArrayList<String>();

	public MethodWithGenericsExample() {
	}

	public List<String> getList() {
		return this.list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}

}
