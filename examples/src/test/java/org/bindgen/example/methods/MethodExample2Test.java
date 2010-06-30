package org.bindgen.example.methods;

import junit.framework.TestCase;

public class MethodExample2Test extends TestCase {

	public void testThreeNames() {
		MethodExample2Binding b = new MethodExample2Binding(new MethodExample2());
		assertEquals("1", b.name().get());
		assertEquals("", b.getName());
		assertEquals("2", b.getNameBinding().get());
	}

}
