package org.bindgen.processor.generators;

import java.util.Collection;

import joist.util.Inflector;

import org.bindgen.processor.util.Util;

public enum AccessorPrefix {

	GET("get", "set"), IS("is", "set"), HAS("has", "set"), NONE("", "");

	/** @return given a getter method name, return which of get/set, is/set, has/set or none we'll use. */
	public static AccessorPrefix guessPrefix(String methodName) {
		for (AccessorPrefix possiblePrefix : values()) {
			String possible = possiblePrefix.getterPrefix;
			if (methodName.startsWith(possible)
				&& methodName.length() > possible.length()
				&& methodName.substring(possible.length(), possible.length() + 1).matches("[A-Z]")) {
				return possiblePrefix;
			}
		}
		return NONE;
	}

	private final String getterPrefix;
	private final String setterPrefix;

	private AccessorPrefix(String getterPrefix, String setterPrefix) {
		this.getterPrefix = getterPrefix;
		this.setterPrefix = setterPrefix;
	}

	/** @return given getFoo/isFoo/hasFoo/foo return setFoo/setFoo/setFoo/foo */
	public String setterName(String getterMethodName) {
		// You can actually have get/set pairs without any prefixes 
		// see {@link NoArgMethodBindingTest} method testPrefixlessAccessors
		return this.setterPrefix + getterMethodName.substring(this.getterPrefix.length());
	}

	/** @return given getFoo/isFoo/hasFoo/foo return "foo" if it is valid, or else the original "getFoo" */
	public String propertyName(Collection<String> namesAlreadyTaken, String getterMethodName) {
		String propertyName = Inflector.uncapitalize(getterMethodName.substring(this.getterPrefix.length()));
		if (Util.isBadPropertyName(propertyName) || namesAlreadyTaken.contains(propertyName)) {
			// Our guess, e.g. getAbstract => abstract, is a keyword, fall back to original getAbstract
			propertyName = getterMethodName;
			// If our getter is getType or hashCode, we need to suffix it
			if (Util.isBindingMethodName(propertyName) || Util.isObjectMethodName(propertyName)) {
				propertyName += "Binding";
			}
		}
		return propertyName;
	}

}
