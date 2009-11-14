package org.bindgen.example;

import java.util.HashSet;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.bindgen.ContainerBinding;

public class CollectionExampleTest extends TestCase {

	@SuppressWarnings("unchecked")
	public void testGetSet() {
		CollectionExample e = new CollectionExample();
		e.things = new HashSet();

		CollectionExampleBinding b = new CollectionExampleBinding(e);
		Assert.assertSame(e.things, b.things().get());
		Assert.assertEquals(null, ((ContainerBinding) b.things()).getContainedType());
	}
}
