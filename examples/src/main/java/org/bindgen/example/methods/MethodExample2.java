package org.bindgen.example.methods;

import org.bindgen.Bindable;

@Bindable
public class MethodExample2 {

	// this takes the b.name()
	public String name() {
		return "1";
	}

	// this cannot fail over to b.getName() as it would overlap with Binding.getName
	public String getName() {
		return "2";
	}

}
