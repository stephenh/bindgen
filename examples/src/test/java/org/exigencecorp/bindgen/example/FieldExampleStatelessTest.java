package org.exigencecorp.bindgen.example;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.bindgen.example.FieldExample;

import bindgen.java.lang.StringBindingPath;
import bindgen.org.bindgen.example.FieldExampleBinding;

public class FieldExampleStatelessTest extends TestCase {

	public void testReadWrite() {
		FieldExampleBinding b = new FieldExampleBinding();
		StringBindingPath<FieldExample> name = b.name();

		FieldExample e1 = new FieldExample("one");
		FieldExample e2 = new FieldExample("two");

		Assert.assertEquals("one", name.getWithRoot(e1));
		Assert.assertEquals("two", name.getWithRoot(e2));

		name.setWithRoot(e1, "one2");
		name.setWithRoot(e2, "two2");

		Assert.assertEquals("one2", e1.name);
		Assert.assertEquals("two2", e2.name);
	}

	public void testPrimitive() {
		FieldExampleBinding b = new FieldExampleBinding();
		FieldExample e1 = new FieldExample("name");
		Assert.assertFalse(e1.good);

		b.good().setWithRoot(e1, true);
		Assert.assertTrue(e1.good);
	}

}
