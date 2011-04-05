package org.bindgen.example;

import org.bindgen.Bindable;
import org.bindgen.example.innerClassExample1.InnerClassBinding;

public class InnerClassExample1 {

	public InnerClass newInnerClass() {
		return new InnerClass();
	}

	@Bindable
	public final class InnerClass {
		public String name;

		public InnerClass() {
		}

		public InnerClassBinding getBind() {
			return new InnerClassBinding(this);
		}

		protected String getBar() {
			return "bar2";
		}
	}

}
