package org.bindgen.processor.inner;

import org.bindgen.Bindable;

public class nastyClass {

	@Bindable
	public static class InnerClass {
		private int x;

		public int getX() {
			return this.x;
		}
		public void setX(int x) {
			this.x = x;
		}
	}

}
