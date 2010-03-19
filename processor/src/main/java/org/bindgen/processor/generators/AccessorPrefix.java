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

	private static final String[] illegalPropertyNames = { "hashCode", "toString", "clone" };
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
		// "get" is because of existing Binding.get method--should probably check clashing with the other Binding methods as well
		if (Util.isJavaKeyword(propertyName) || "get".equals(propertyName) || namesAlreadyTaken.contains(propertyName)) {
			if (this == NONE) {
				// Note that this case can be reached using the following scenario:
				// public int getXyz();
				// public void setXyz();
				// public String xyz();
				// and the current implementation drops the binding for xyz()
				// see {@link NoArgMethodBindingTest} method testGenerateBindableHiding
				// TODO maybe we could add a suffix here (like below) to avoid returning null (which is a bad idea in general)
				// 
				// however it should be smart, not just xyzBinding, if it's a field/accessor it should be one thing
				// if it's a no-arg method it should be something else, it should be independent of textual member ordering 
				// 
				return null;
			}
			// Our guess, e.g. getAbstract => abstract, is a keyword, fall back to original getAbstract
			propertyName = getterMethodName;
		}
		for (String illegalProp : illegalPropertyNames) {
			if (illegalProp.equals(propertyName)) {
				// Our guess, e.g. toString is illegal - because according to Object it should return a String not a XyzBinding, 
				// so fall back to original toString name (e.g. {get/is/has}toString)
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
