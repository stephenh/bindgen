package org.bindgen.example;

import org.bindgen.Bindable;

@Bindable
public class ObjectExample {

	public Object value;
	public Class<?> clazz;

	public Class<?> getOtherClazz() {
		return null;
	}
}
