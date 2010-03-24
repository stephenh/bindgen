package org.bindgen.example.inheritance3;

public class Base<T> {

	private T value;

	public T value() {
		return this.value;
	}

	public Base<T> value(T value) {
		this.value = value;
		return this;
	}

}
