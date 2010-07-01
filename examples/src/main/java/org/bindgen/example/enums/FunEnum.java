package org.bindgen.example.enums;

import org.bindgen.Bindable;

@Bindable
public enum FunEnum {
	FIRST(100), SECOND(50);

	private int funLevel;

	private FunEnum(int newFunLevel) {
		this.funLevel = newFunLevel;
	}

	public int getFunLevel() {
		return this.funLevel;
	}

	public void setFunLevel(int newFunLevel) {
		this.funLevel = newFunLevel;
	}
}
