package org.bindgen.example.inheritance3;

import junit.framework.TestCase;

public class ChildTest extends TestCase {

	public void testChild() {
		Child c = new Child();
		ChildBinding b = new ChildBinding(c);
		b.value().set("foo");
	}

}
