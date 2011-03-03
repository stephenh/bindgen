package org.bindgen.example.wildcards;

import org.bindgen.Bindable;

@Bindable
public class Test1 {

	public Test2<?> a;
	// public Test2<? extends Test2<?>> b;

}
