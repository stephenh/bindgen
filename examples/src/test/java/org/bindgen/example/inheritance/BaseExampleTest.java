package org.bindgen.example.inheritance;

import junit.framework.Assert;
import junit.framework.TestCase;

public class BaseExampleTest extends TestCase {

	public void testSubBindings() {
		SubExample sub = new SubExample();
		SubExampleBinding subb = new SubExampleBinding(sub);
		subb.name().set("foo");
		subb.subOnly().set("bar");

		Assert.assertEquals("foo", sub.name);
		Assert.assertEquals("bar", sub.subOnly);

		// because of the clash with the base class 'name', we get an extra 'nameField' that still points to 'SubExample.name'
		subb.nameField().set("foo");
		Assert.assertEquals("foo", sub.name);

		// 5 == base description, sub name, sub subOnly, hashCode and toString
		// +1 currently for the parent name and child nameField
		Assert.assertEquals(6, subb.getChildBindings().size());
	}

	public void testSubBindingsWithRealSub() {
		SubExampleBinding subb = new SubExampleBinding();
		subb.set(new SubExample());
		Assert.assertEquals(null, subb.subOnly().get());
	}

	public void testSubBindingsWithBase() {
		// SubExampleBinding subb = new SubExampleBinding();
		try {
			// subb.set(new BaseExample());
			// Assert.fail();
		} catch (ClassCastException cce) {
			// Okay
		}
	}

}
