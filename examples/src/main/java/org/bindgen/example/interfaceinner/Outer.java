package org.bindgen.example.interfaceinner;

import org.bindgen.Bindable;

public interface Outer {
	@Bindable
	public static class Inner {
		private String something;

		public String getSomething() {
			return this.something;
		}

		public void setSomething(String something) {
			this.something = something;
		}
	}
}
