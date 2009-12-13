package org.bindgen.processor;

import static org.junit.Assert.*;

import org.junit.Test;

public class NoArgMethodBindingTest extends AbstractBindgenTestCase {

	@Test
	public void testGenerateBindingsForNoArgMethodsThatReturnAValue() throws Exception {
		ClassLoader loader = this.compile("org/bindgen/processor/noarg/ComplexData.java");

		//Class<?> actualClass = loader.loadClass("org.bindgen.processor.noarg.ComplexData");
		Class<?> bindingClass = loader.loadClass("org.bindgen.processor.noarg.ComplexDataBindingPath");

		assertNotNull(bindingClass);
		assertMethodDeclared(bindingClass, "noArgNoThrows");
		assertMethodNotDeclared(bindingClass, "noArgWithThrows");
		assertMethodNotDeclared(bindingClass, "oneArgNoThrows");
		assertMethodNotDeclared(bindingClass, "oneArgWithThrows");
	}
}
