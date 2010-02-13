package org.bindgen.processor.noarg;

import org.bindgen.Bindable;

@Bindable
public class ComplexData {
	private int x, y;
	private int sumCount = 0;
	
	public ComplexData(int x, int y) {
		this.x=x;
		this.y=y;
	}
	
	public int noArgNoThrows() {
		sumCount++;
		return x+y;
	}

	public int noArgWithThrows() throws Exception {
		return 0;
	}

	public int oneArgNoThrows(int arg) {
		return arg;
	}

	public int oneArgWithThrows(int arg) throws Exception {
		return arg;
	}
	
	public int getSumCount() {
		return this.sumCount;
	}
	
	public int getX() {
		return this.x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return this.y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
}