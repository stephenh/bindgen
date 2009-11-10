package org.bindgen.example;

import java.util.List;
import java.util.Set;

import org.bindgen.Bindable;

@Bindable
public class GenericsExample<T> {

	private T foo;
	public T bar;

	public T getFoo() {
		return this.foo;
	}

	public void setFoo(T foo) {
		this.foo = foo;
	}

	public List<? extends Set<?>> getSets() {
		return null;
	}

	// Was causing unnecessary cast errors because the ? triggers "hasWildcards()"
	public List<Set<?>> getSets4() {
		return null;
	}

}
