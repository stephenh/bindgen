package org.bindgen;

import junit.framework.TestCase;

import org.bindgen.binding.GenericObjectBindingPath;

import bindgen.org.bindgen.inscope.PersonBinding;

public class ScopeTest extends TestCase {
	
	public void testShouldGenerateGenericBindingForOutOfScopeProperty() throws Exception {
		final Class<?> generic = GenericObjectBindingPath.class;
		final Class<?> binding = new PersonBinding().address().getClass();
		assertTrue(generic.isAssignableFrom(binding));
	}
	
}
