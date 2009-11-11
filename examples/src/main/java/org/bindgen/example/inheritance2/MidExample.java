package org.bindgen.example.inheritance2;

import org.bindgen.Bindable;

@Bindable
public class MidExample<T extends MidExample<T>> extends BaseExample<T> {

	public String name;
	public String subOnly;

}
