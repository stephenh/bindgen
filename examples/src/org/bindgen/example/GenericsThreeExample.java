package org.bindgen.example;

import java.util.List;

import org.bindgen.Bindable;

@Bindable
public class GenericsThreeExample<T> {

	private List<String> strings;
	private List<T> things;
	// Caused errors with ContainerBinding because of T
	public List<T> list;

	public List<T> getThings() {
		return this.things;
	}

	public List<String> getStrings() {
		return this.strings;
	}

}
