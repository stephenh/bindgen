package org.bindgen.example.interfaceinner;

import junit.framework.TestCase;

import org.bindgen.example.interfaceinner.outer.InnerBinding;

public class BindingTest extends TestCase {
	public void testInnerBinding() {
		Object binding = new InnerBinding().something();
	}
}
