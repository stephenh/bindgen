package org.bindgen.processor.inner;

import org.bindgen.Bindable;

public class SomeClass {
	
	@Bindable
	public static class InnerClass {
		private int x;

		public int getX() {
			return this.x;
		}
		public void setX(int x) {
			this.x = x;
		}
		
		public int squared() {
			return x*x;
		}
	}
	
	private int y;
	
	public int half() {
		return y/2;
	}

	public int getY() {
		return this.y;
	}
	public void setY(int y) {
		this.y = y;
	}
}