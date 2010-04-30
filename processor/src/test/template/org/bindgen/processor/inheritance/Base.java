package org.bindgen.processor.inheritance;

import java.util.List;

public class Base<T> {
	public T value;

	public T value() {
		return this.value;
	}

	public void value(T value) {
		this.value = value;
	}

	public List<T> list() {
		return null;
	}

}