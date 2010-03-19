package org.bindgen.example.access;

import junit.framework.TestCase;

public class ClashesTest extends TestCase {

	public void testEach() {
		Clashes c = new Clashes();
		c.a = "A";

		ClashesBinding b = new ClashesBinding(c);

		// getter/setter
		assertEquals("from getter A", b.getA().get());
		b.getA().set("B");
		assertEquals("B by setter", c.a);

		// reset
		c.a = "A";

		// prefixless
		assertEquals("from prefixless A", b.a().get());
		b.a().set("B");
		assertEquals("B by prefixless", c.a);

		// reset
		c.a = "A";

		// field
		assertEquals("A", b.aField().get());
		b.aField().set("B");
		assertEquals("B", c.a);
	}

}
