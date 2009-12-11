package org.bindgen.processor;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;

public class NoArgMethodBindingTest extends AbstractBindgenTestCase {

	@Test
	public void shouldGenerateBindingsNoArgMethodsThatReturnAValue() throws Exception {
		ClassLoader loader = this.compile("org/bindgen/processor/noarg/ComplexData.java");

		//Class<?> actualClass = loader.loadClass("org.bindgen.processor.cache.ComplexData");
		Class<?> bindingClass = loader.loadClass("org.bindgen.processor.noarg.ComplexDataBindingPath");

		Method sum = bindingClass.getDeclaredMethod("sum");
		assertNotNull(sum);
	}
}
