package org.bindgen.processor.generators;

import joist.util.Inflector;

public enum AccessorPrefix {

	NONE("", ""), GET("get", "set"), IS("is", "set"), HAS("has", "set");

	private final String getterPrefix;
	private final String setterPrefix;

	private AccessorPrefix(String getterPrefix, String setterPrefix) {
		this.getterPrefix = getterPrefix;
		this.setterPrefix = setterPrefix;
	}

	/** @return "foo" given getFoo/isFoo/hasFoo/foo */
	public String preferredPropertyName(String getterMethodName) {
		return Inflector.uncapitalize(getterMethodName.substring(this.getterPrefix.length()));
	}

	/** @return "setFoo"/setFoo/setFoo/foo given getFoo/isFoo/hasFoo/foo */
	public String setterName(String getterMethodName) {
		// We can have get/set pairs without any prefixes, see {@link NoArgMethodBindingTest} testPrefixlessAccessors()
		return this.setterPrefix + getterMethodName.substring(this.getterPrefix.length());
	}

	/** @return true if this prefix is at the start of <code>methodName</code> */
	public boolean matches(String methodName) {
		return methodName.startsWith(this.getterPrefix)
			&& methodName.length() > this.getterPrefix.length()
			&& (this == NONE || methodName.substring(this.getterPrefix.length(), this.getterPrefix.length() + 1).matches("[A-Z]"))
			&& (this != NONE || !this.hasOtherPrefix(methodName));
	}

	/** @return true if a prefix other than us would also claim this <code>methodName</code> */
	private boolean hasOtherPrefix(String methodName) {
		for (AccessorPrefix p : values()) {
			if (p != this && p.matches(methodName)) {
				return true;
			}
		}
		return false;
	}

}
