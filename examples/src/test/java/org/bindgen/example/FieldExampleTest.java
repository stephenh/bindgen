package org.bindgen.example;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.bindgen.ContainerBinding;

public class FieldExampleTest extends TestCase {

	public void testReadWrite() {
		FieldExample e = new FieldExample("name");
		FieldExampleBinding b = new FieldExampleBinding(e);

		Assert.assertEquals("name", b.name().get());

		b.name().set("name1");
		Assert.assertEquals("name1", e.name);
	}

	public void testListReadWrite() {
		FieldExample e = new FieldExample("name");
		FieldExampleBinding b = new FieldExampleBinding(e);

		List<String> list = b.list().get();
		list.add("foo");

		Assert.assertEquals("foo", e.list.get(0));
		Assert.assertSame(list, b.list().get());
		Assert.assertEquals(String.class, ((ContainerBinding) b.list()).getContainedType());
	}

	public void testPrimitive() {
		FieldExample e = new FieldExample("name");
		FieldExampleBinding b = new FieldExampleBinding(e);
		Assert.assertFalse(b.good().get());

		b.good().set(true);
		Assert.assertTrue(e.good);
	}

	public void testOneCharge() {
		FieldExample e = new FieldExample("name");
		FieldExampleBinding b = new FieldExampleBinding(e);
		Assert.assertEquals(null, b.f().get());
		b.f().set("foo");
		Assert.assertEquals("foo", e.f);
	}

	public void testGet() {
		FieldExample e = new FieldExample("name");
		e.get = true;
		FieldExampleBinding b = new FieldExampleBinding(e);
		Assert.assertEquals(true, b.getField().get().booleanValue());
	}

}
