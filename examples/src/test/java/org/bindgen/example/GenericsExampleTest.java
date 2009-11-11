package org.bindgen.example;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.bindgen.example.GenericsExample;

import bindgen.org.bindgen.example.GenericsExampleBinding;

public class GenericsExampleTest extends TestCase {

	public void testField() {
		GenericsExample<String> ge = new GenericsExample<String>();
		GenericsExampleBinding<String> geb = new GenericsExampleBinding<String>(ge);

		ge.bar = "1";
		Assert.assertEquals("1", geb.bar().get());

		geb.bar().set("2");
		Assert.assertEquals("2", ge.bar);
	}

	public void testMethod() {
		GenericsExample<String> ge = new GenericsExample<String>();
		GenericsExampleBinding<String> geb = new GenericsExampleBinding<String>(ge);

		ge.setFoo("1");
		Assert.assertEquals("1", geb.foo().get());

		geb.foo().set("2");
		Assert.assertEquals("2", ge.getFoo());
	}

}
