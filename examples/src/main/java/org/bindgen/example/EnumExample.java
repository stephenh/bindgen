package org.bindgen.example;

import org.bindgen.Bindable;

@Bindable
public class EnumExample {

	public Foo foo;

	public enum Foo {
		ONE, TWO
	}

}
