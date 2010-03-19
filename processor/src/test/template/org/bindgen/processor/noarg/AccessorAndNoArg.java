package org.bindgen.processor.noarg;

import org.bindgen.Bindable;

@Bindable
public class AccessorAndNoArg {
	// case 1: the accessors hide the no-arg with the same name
	// in this case the no-arg wasn't generated 
	public int getFoobar() {
		return +1;
	}
	public void setFoobar(int foo) {
		
	}

	public String foobar() {
		return "unrelated to above";
	}
	
	// case 2: the no-arg prevents the accessor from binding to "barfoo" and forces them to bind to "getBarFoo"
	public String barfoo() {
		return "not related to below";
	}
	public int getBarfoo() {
		return +1;
	}
	
	public void setBarfoo(int foo) {
		
	}
	
}