package org.bindgen.example;

import org.bindgen.Bindable;

public class Parents {

	@Bindable
	public static class Foo {
		public String bar;

		public String getBaz() {
			return "baz";
		}

		public String toString() {
			return "foo";
		}
	}

	@Bindable
	public static class FooChild {
		public Foo foo;

		public String toString() {
			return "child";
		}
	}

	@Bindable
	public static class FooPage {
		public Foo foo;
	}

	@Bindable
	public static class Zaz {
		public String name;
	}

}
