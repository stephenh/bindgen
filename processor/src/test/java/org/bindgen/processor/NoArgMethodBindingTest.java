package org.bindgen.processor;

import static org.junit.Assert.*;

import org.junit.Test;

public class NoArgMethodBindingTest extends AbstractBindgenTestCase {

	@Test
	public void testGenerateBindingsForNoArgMethodsThatReturnAValue() throws Exception {
		String testedClass = "org.bindgen.processor.noarg.ComplexData";
		ClassLoader loader = this.compile(filePath(testedClass));

		//Class<?> actualClass = loader.loadClass("org.bindgen.processor.noarg.ComplexData");
		Class<?> bindingClass = loader.loadClass(testedClass + "BindingPath");

		assertNotNull(bindingClass);
		assertMethodDeclared(bindingClass, "noArgNoThrows");
		assertMethodNotDeclared(bindingClass, "noArgWithThrows");
		assertMethodNotDeclared(bindingClass, "oneArgNoThrows");
		assertMethodNotDeclared(bindingClass, "oneArgWithThrows");
	}

}
