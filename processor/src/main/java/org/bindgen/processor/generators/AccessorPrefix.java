package org.bindgen.processor.generators;

import joist.util.Inflector;

import org.bindgen.processor.util.Util;

public enum AccessorPrefix {

	// these accessors use the default behavior
	GET("get", "set"),
	IS("is", "set"),
	HAS("has", "set"),

	// none overrides the default accessor behavior
	NONE("", "") {
		@Override
		public String setterName(String getterMethodName) {
			return getterMethodName;
		}

		@Override
		public String propertyName(String getterMethodName) {
			if (Util.isJavaKeyword(getterMethodName) || "get".equals(getterMethodName)) {
				return null;
			}
			for (String illegalProp : AbstractMethodBindingGenerator.illegalPropertyNames) {
				if (illegalProp.equals(getterMethodName)) {
					return getterMethodName + "Binding";
				}
			}
			return getterMethodName;
		}
	};

	/** @return given a getter method name, return which of get/set, is/set, has/set or none we'll use. */
	public static AccessorPrefix guessPrefix(String methodName) {
		AccessorPrefix[] values = values();
		for (int i = 1; i < values.length; i++) {
			AccessorPrefix possiblePrefix = values[i];
			String possible = possiblePrefix.getterPrefix;
			if (methodName.startsWith(possible)
				&& methodName.length() > possible.length()
				&& methodName.substring(possible.length(), possible.length() + 1).matches("[A-Z]")) {
				return possiblePrefix;
			}
		}
		return NONE;
	}

	public final String getterPrefix, setterPrefix;

	private AccessorPrefix(String getterPrefix, String setterPrefix) {
		this.setterPrefix = setterPrefix;
		this.getterPrefix = getterPrefix;
	}

	public String setterName(String getterMethodName) {
		return this.setterPrefix + getterMethodName.substring(this.getterPrefix.length());
	}

	public String propertyName(String getterMethodName) {
		String propertyName = Inflector.uncapitalize(getterMethodName.substring(this.getterPrefix.length()));
		if (Util.isJavaKeyword(propertyName) || "get".equals(propertyName)) {
			propertyName = getterMethodName;
		}
		for (String illegalProp : AbstractMethodBindingGenerator.illegalPropertyNames) {
			if (illegalProp.equals(propertyName)) {
				propertyName = getterMethodName;
				break;
			}
		}
		return propertyName;
	}

}
