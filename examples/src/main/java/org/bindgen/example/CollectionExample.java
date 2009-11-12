package org.bindgen.example;

import java.util.Set;

import org.bindgen.Bindable;

@Bindable
public class CollectionExample {
	// Was causing an error in getContainedType because of not having generics
	@SuppressWarnings("unchecked")
	public Set things;
}
