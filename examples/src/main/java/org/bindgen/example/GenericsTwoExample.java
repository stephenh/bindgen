package org.bindgen.example;

import java.util.List;

import org.bindgen.Bindable;

@Bindable
public class GenericsTwoExample<T extends List<?>> {

	private T foo;
	public T bar;

	public T getFoo() {
		return this.foo;
	}

	public void setFoo(T foo) {
		this.foo = foo;
	}

}
