package org.bindgen.example.inheritance4;

import org.bindgen.Bindable;

@Bindable
public interface Child extends Parent<String> {
	String getChildField();

	void setChildField(String field);
}
