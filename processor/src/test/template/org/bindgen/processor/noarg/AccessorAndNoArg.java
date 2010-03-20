package org.bindgen.processor.noarg;

import org.bindgen.Bindable;

@Bindable
public class AccessorAndNoArg {
	// case 1: the prefix accessors come before the prefix-less accessors
	public String getFoo1() {
		return "1a";
	}
	public void setFoo1(String foo1) {
	}
	public String foo1() {
		return "1b";
	}
	public AccessorAndNoArg foo1(String foo1) {
		return this;
	}
	
	// case 2: the prefix-less accessors come before the prefix accessors
	public String foo2() {
		return "2a";
	}
	public AccessorAndNoArg foo2(String foo2) {
		return this;
	}
	public String getFoo2() {
		return "2b";
	}
	public void setFoo2(String foo2) {
	}
	
	// case 3: the prefix getter comes before the prefix-less getter
	public String getFoo3() {
		return "3a";
	}
	public String foo3() {
		return "3b";
	}

	// case 4: the prefix-less getter comes before the prefix getter
	public String foo4() {
		return "4a";
	}
	public String getFoo4() {
		return "4b";
	}
	

}