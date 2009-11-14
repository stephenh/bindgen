package org.bindgen;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.bindgen.binding.GenericObjectBindingPath;
import org.bindgen.inscope.Person;
import org.bindgen.inscope.PersonBinding;
import org.bindgen.outofscope.Address;

public class ScopeTest extends TestCase {

	public void testShouldGenerateGenericBindingForOutOfScopeProperty() throws Exception {
		final Class<?> generic = GenericObjectBindingPath.class;
		final Class<?> binding = new PersonBinding().address().getClass();
		assertTrue(generic.isAssignableFrom(binding));
	}

	public void testInnerClassIsStillTypeSafe() {
		Person p = new Person();
		PersonBinding b = new PersonBinding(p);

		Address a = new Address();
		b.address().set(a);
		Assert.assertSame(a, b.address().get());
	}
}
