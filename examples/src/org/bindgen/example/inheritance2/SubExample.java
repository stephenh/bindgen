package org.bindgen.example.inheritance2;

import org.bindgen.Bindable;

@Bindable
public class SubExample extends MidExample<SubExample> {

	public String name;
	public String subOnly;

}
