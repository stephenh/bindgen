package org.bindgen.example.interfaceinner;

import org.bindgen.Bindable;

public interface Outer {
	@Bindable
	public static class Inner {
		public String getSomething() {
			return "Something";
		}

		public void setSomething(String something) {
			// No-op. This is just a test! :-)
		}
	}
}
