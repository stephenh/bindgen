package org.bindgen.processor.noarg;

import org.bindgen.Bindable;

@Bindable
public class NoPrefixAccessors {

	private Integer foo = 123;
	public Integer foo() {
		return foo;
	}

	public NoPrefixAccessors foo(Integer i) {
		foo = i;
		return this;
	}
	
	
	private Integer foofoo = 123;
	public Integer foofoo() {
		return foofoo;
	}

	public NoPrefixAccessors foofoo(Integer i) {
		foofoo = i;
		return this;
	}
	
}