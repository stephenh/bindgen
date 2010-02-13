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
		// 5 == base description, sub name, sub subOnly, hashCode and toString
		Assert.assertEquals(5, subb.getChildBindings().size());
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
