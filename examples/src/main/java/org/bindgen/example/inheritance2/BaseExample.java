package org.bindgen.example.inheritance2;

import org.bindgen.Bindable;

@Bindable
public class BaseExample<T extends BaseExample<T>> {

	public String name;
	public String description;

}
