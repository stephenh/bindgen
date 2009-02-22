package org.exigencecorp.bindgen.example.skipAttributes;

import org.exigencecorp.bindgen.Bindable;

@Bindable
public class SkipAttributesExample {

	// If this attribute got generated, we'd see a warning
	@Deprecated
	public String getName() {
		return null;
	}

	// If this attribute got generated, we'd see a warning
	@Deprecated
	public String description;

	@Deprecated
	public void doSomething() {
	}

}
