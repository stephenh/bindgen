package org.bindgen.processor;

import static org.junit.Assert.*;


import org.junit.Test;

public class AccessTest extends AbstractBindgenTestCase {

	@Test
	public void shouldGenerateBindingsForNonPublicAnnotatedFields() throws Exception {
		ClassLoader loader = this.compile("org/bindgen/processor/access/AccessBean.java");

		Class<?> clazz = loader.loadClass("org.bindgen.processor.access.AccessBeanBindingPath");

		assertPublic(clazz.getDeclaredMethod("publicField"));
		assertProtected(clazz.getDeclaredMethod("protectedField"));
		assertPackage(clazz.getDeclaredMethod("packageField"));

		assertPublic(clazz.getDeclaredMethod("publicMethod"));
		assertProtected(clazz.getDeclaredMethod("protectedMethod"));
		assertPackage(clazz.getDeclaredMethod("packageMethod"));

	}

	@Test
	public void shouldNotGenerateBindingsForPrivateFields() throws Exception {
		ClassLoader loader = this.compile("org/bindgen/processor/access/AccessBean.java");

		Class<?> clazz = loader.loadClass("org.bindgen.processor.access.AccessBeanBindingPath");

		try {
			clazz.getDeclaredMethod("privateField");
			fail();
		} catch (NoSuchMethodException e) {
			//noop
		}
		try {
			clazz.getDeclaredMethod("privateMethod");
			fail();
		} catch (NoSuchMethodException e) {
			//noop
		}
	}

	@Test
	public void shouldNotGenerateBindingsForInheritedNonPublicFields() throws Exception {
		ClassLoader loader = this.compile("org/bindgen/processor/access/package1/Bean1.java", "org/bindgen/processor/access/package2/Bean2.java");
	}

	@Test(expected = ClassNotFoundException.class)
	public void shouldNotGenerateBindingsForClassesInDefaultPackage() throws Exception {
		ClassLoader loader = this.compile("ClassInDefaultPackage.java");
		loader.loadClass("ClassInDefaultPackageBindingPath");
		fail();
	}

}
