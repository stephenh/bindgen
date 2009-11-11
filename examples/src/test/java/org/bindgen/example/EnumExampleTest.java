package org.bindgen.example;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.bindgen.example.EnumExample;
import org.bindgen.example.EnumExample.Foo;

import bindgen.org.bindgen.example.EnumExampleBinding;

public class EnumExampleTest extends TestCase {

	public void testEnum() {
		EnumExample e = new EnumExample();
		EnumExampleBinding b = new EnumExampleBinding(e);
		b.foo().set(Foo.ONE);
		Assert.assertEquals(Foo.ONE, e.foo);
	}

}
