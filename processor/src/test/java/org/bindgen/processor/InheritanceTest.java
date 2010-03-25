package org.bindgen.processor;

import org.junit.Assert;
import org.junit.Test;

/** Tests a child class inheritance a generic getter/setter. */
public class InheritanceTest extends AbstractBindgenTestCase {

	@Test
	public void testChild() throws Exception {
		ClassLoader cl = this.compile("org/bindgen/processor/inheritance/Base.java", "org/bindgen/processor/inheritance/Child.java");

		Class<?> cClass = cl.loadClass("org.bindgen.processor.inheritance.Child");
		Class<?> cbClass = cl.loadClass("org.bindgen.processor.inheritance.ChildBinding");
		assertChildBindings(cbClass, "hashCodeBinding", "toStringBinding", "value", "valueField");

		Object child = cClass.newInstance();
		Object childBinding = cbClass.getConstructor(cClass).newInstance(child);
		Object valueBinding = cbClass.getMethod("value").invoke(childBinding);
		Object valueFieldBinding = cbClass.getMethod("valueField").invoke(childBinding);

		// set via the binding
		valueBinding.getClass().getMethod("set", String.class).invoke(valueBinding, "FOO");
		// get via the class
		Assert.assertEquals("FOO", cClass.getMethod("value").invoke(child));

		// set via the class
		cClass.getMethod("value", Object.class).invoke(child, "BAR");
		// get via the binding
		Assert.assertEquals("BAR", valueBinding.getClass().getMethod("get").invoke(valueBinding));

		// set via the field binding
		valueFieldBinding.getClass().getMethod("set", String.class).invoke(valueFieldBinding, "ZAZ");
		// get via the field binding
		Assert.assertEquals("ZAZ", valueFieldBinding.getClass().getMethod("get").invoke(valueFieldBinding));
	}

}
