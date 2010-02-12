package org.bindgen.example.inheritance;

import junit.framework.Assert;
import junit.framework.TestCase;

public class InterfaceInheritanceTest extends TestCase {

	public void testA() {
		InterfaceBBinding b = new InterfaceBBinding();
		b.set(new InterfaceBImpl());
		Assert.assertEquals("a", b.fromA().get());
		Assert.assertEquals("aa", b.fromAA().get());
		Assert.assertEquals("b", b.fromB().get());
	}

}
