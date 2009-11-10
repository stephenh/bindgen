package org.exigencecorp.bindgen.example;

import org.bindgen.example.InnerClassExample1;
import org.bindgen.example.InnerClassExample2;

import junit.framework.Assert;
import junit.framework.TestCase;

public class InnerClassExampleTest extends TestCase {

	public void testInner1() {
		InnerClassExample1 foo = new InnerClassExample1();
		InnerClassExample1.InnerClass fooInner = foo.newInnerClass();
		fooInner.getBind().name().set("set");
		Assert.assertEquals("set", fooInner.name);
	}

	public void testInner2() {
		InnerClassExample2 foo = new InnerClassExample2();
		InnerClassExample2.InnerClass fooInner = foo.newInnerClass();
		fooInner.getBind().different().set("set");
		Assert.assertEquals("set", fooInner.different);
	}

}
