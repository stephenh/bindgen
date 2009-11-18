package org.bindgen.example.access;

import org.bindgen.Bindable;

@Bindable
public class Bean {
	@SuppressWarnings("unused")
	private String privateField;

	public String publicField;

	@Bindable
	protected String protectedField;

	@Bindable
	String packageField;

}
