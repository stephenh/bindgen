package org.bindgen;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.bindgen.binding.GenericObjectBindingPath;
import org.bindgen.inscope.AddressIn;
import org.bindgen.inscope.Person;
import org.bindgen.inscope.PersonBinding;
import org.bindgen.outofscope.AddressOut;

public class ScopeTest extends TestCase {

	public void testShouldGenerateGenericBindingForOutOfScopeProperty() throws Exception {
		final Class<?> generic = GenericObjectBindingPath.class;
		final Class<?> binding = new PersonBinding().addressOut().getClass();
		assertTrue(generic.isAssignableFrom(binding));
	}

	public void testInnerClassIsStillTypeSafe() {
		Person p = new Person();
		PersonBinding b = new PersonBinding(p);

		AddressOut a = new AddressOut();
		b.addressOut().set(a);
		Assert.assertSame(a, b.addressOut().get());

		Assert.assertEquals(AddressOut.class, b.addressOut().getType());
	}

	public void testWithinScopeIsGenerated() {
		Person p = new Person();
		PersonBinding b = new PersonBinding(p);

		AddressIn a = new AddressIn();
		b.addressIn().set(a);
		Assert.assertSame(a, b.addressIn().get());

		Assert.assertEquals(AddressIn.class, b.addressIn().getType());

		b.addressIn().city().set("Foo");
	}
}
