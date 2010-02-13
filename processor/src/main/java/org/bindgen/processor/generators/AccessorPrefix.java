package org.bindgen.processor.generators;

import java.util.List;

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

	private static final String[] illegalPropertyNames = { "hashCode", "toString", "clone" };
	private final String getterPrefix;
	private final String setterPrefix;

	private AccessorPrefix(String getterPrefix, String setterPrefix) {
		this.getterPrefix = getterPrefix;
		this.setterPrefix = setterPrefix;
	}

	/** @return given getFoo/isFoo/hasFoo/foo return setFoo/setFoo/setFoo/null */
	public String setterName(String getterMethodName) {
		if (this == NONE) {
			return null;
		}
		return this.setterPrefix + getterMethodName.substring(this.getterPrefix.length());
	}

	/** @return given getFoo/isFoo/hasFoo/foo return "foo" if it is valid, or else the original "getFoo" */
	public String propertyName(List<String> namesAlreadyTaken, String getterMethodName) {
		String propertyName = Inflector.uncapitalize(getterMethodName.substring(this.getterPrefix.length()));
		// "get" is because of existing Binding.get method--should probably check clashing with the other Binding methods as well
		if (Util.isJavaKeyword(propertyName) || "get".equals(propertyName) || namesAlreadyTaken.contains(propertyName)) {
			if (this == NONE) {
				return null;
			}
			// Our guess, e.g. getAbstract => abstract, is a keyword, fall back to original getAbstract
			propertyName = getterMethodName;
		}
		for (String illegalProp : illegalPropertyNames) {
			if (illegalProp.equals(propertyName)) {
				// Our guess, e.g. toString => string, is illegal, fall back to original toString
				propertyName = getterMethodName;
				if (this == NONE) {
					// NONE means propertyName==getterMethodName, so add a suffix to avoid clash
					propertyName += "Binding";
				}
				break;
			}
		}
		return propertyName;
	}

}
