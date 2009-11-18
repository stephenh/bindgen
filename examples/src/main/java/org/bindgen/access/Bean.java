package org.bindgen.access;

import org.bindgen.Bindable;

@Bindable
public class Bean {
	public String publicField;

	@Bindable
	protected String protectedField;

	@Bindable
	String packageField;

}
