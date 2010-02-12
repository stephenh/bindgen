package org.bindgen.processor.basic;

import org.bindgen.Bindable;

@Bindable
public class Point {
	public int x;
	private int y,z;
	
	public int getY() {
		return y;
	}

	public int getZero() {
		return 0;
	}

	public Point getOrigin() {
		return new Point();
	}

	
	public int getZ() {
		return this.z;
	}
	public void setZ(int z) {
		this.z = z;
	}
	
}
