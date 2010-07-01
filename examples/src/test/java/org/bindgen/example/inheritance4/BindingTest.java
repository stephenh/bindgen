package org.bindgen.example.inheritance4;

import junit.framework.TestCase;

public class BindingTest extends TestCase {
	public void testInheritedBinding() {
		Object binding = new ChildBinding().parentField();
	}
}
