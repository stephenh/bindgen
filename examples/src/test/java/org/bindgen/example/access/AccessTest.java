package org.bindgen.example.access;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import junit.framework.Assert;
import junit.framework.TestCase;

public class AccessTest extends TestCase {

	public void testPackageAccess() throws Exception {
		Method m = BeanBindingPath.class.getDeclaredMethod("packageField");
		Assert.assertEquals(false, Modifier.isPublic(m.getModifiers()));
		Assert.assertEquals(false, Modifier.isPrivate(m.getModifiers()));
		Assert.assertEquals(false, Modifier.isProtected(m.getModifiers()));
	}

	public void testProtectedAccess() throws Exception {
		Method m = BeanBindingPath.class.getDeclaredMethod("protectedField");
		Assert.assertEquals(true, Modifier.isProtected(m.getModifiers()));
	}

	public void testPublicAccess() throws Exception {
		Method m = BeanBindingPath.class.getDeclaredMethod("publicField");
		Assert.assertEquals(true, Modifier.isPublic(m.getModifiers()));
	}

	public void testPrivateDoesNotGetExposed() throws Exception {
		try {
			BeanBindingPath.class.getDeclaredMethod("privateField");
			fail();
		} catch (NoSuchMethodException nsme) {
			// expected
		}
	}

}
