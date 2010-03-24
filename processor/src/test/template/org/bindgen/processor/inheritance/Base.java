package org.bindgen.processor.inheritance;

public class Base<T> {
	public T value;

	public T value() {
		return this.value;
	}

	public void value(T value) {
		this.value = value;
	}
}