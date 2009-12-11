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
	
	public int sum() {
		sumCount++;
		return x+y;
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