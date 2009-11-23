package org.bindgen.processor;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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

	private static void assertPublic(Method method) {
		if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
			fail();
		}
	}

	private static void assertProtected(Method method) {
		if ((method.getModifiers() & Modifier.PROTECTED) == 0) {
			fail();
		}
	}

	private static void assertPackage(Method method) {
		if ((method.getModifiers() & Modifier.PUBLIC) > 0
			|| (method.getModifiers() & Modifier.PROTECTED) > 0
			|| (method.getModifiers() & Modifier.PRIVATE) > 0) {
			fail();
		}
	}

}
