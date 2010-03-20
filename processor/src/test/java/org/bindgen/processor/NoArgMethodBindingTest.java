package org.bindgen.processor;

import static org.junit.Assert.*;

import org.bindgen.Binding;
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

	@Test
	public void testGenerateWithTwoPrefixes() throws Exception {
		String testedClass = "org.bindgen.processor.noarg.GetNoArgData";
		Class<?> bindingPathClass = this.compile(filePath(testedClass)).loadClass(testedClass + "BindingPath");
		Class<?> bindingClass = this.compile(filePath(testedClass)).loadClass(testedClass + "Binding");

		assertNotNull(bindingPathClass);
		assertMethodDeclared(bindingPathClass, "foo");
		assertMethodDeclared(bindingPathClass, "isFoo");

		assertChildBindings(bindingClass, "toStringBinding", "hashCodeBinding", "foo", "isFoo");
	}

	@Test
	public void testGenerateBindableHiding() throws Exception {
		String testedClass = "org.bindgen.processor.noarg.AccessorAndNoArg";
		Class<?> bindingClass = this.compile(filePath(testedClass)).loadClass(testedClass + "Binding");

		assertChildBindings(
			bindingClass,
			"foo1",
			"foo2",
			"foo3",
			"foo4",
			"foo5",
			"foo6",
			"getFoo1",
			"getFoo2",
			"getFoo3",
			"getFoo4",
			"getFoo5",
			"isFoo6",
			"toStringBinding",
			"hashCodeBinding");
		// Note that a a simple reordering of members used to cause inconsistent binding generation, for example:
		// the class has two accessors: getFoobar/setFoobar for "foobar" and getBarfoo/setBarFoo for "barfoo"
		// and two unrelated but misleadingly named no-arg methods: "fobar" and "barfoo"
		// only three bindings used to result and they would be named inconsistenttly. 
	}

	@Test
	public void testPrefixlessAccessors() throws Exception {
		/* In some places, where java is used as a domain specific language
		 * you can actually meet accessor methods that do not have any prefixes (e.g. no get/is/has/set)
		 */
		String testedClassName = "org.bindgen.processor.noarg.NoPrefixAccessors";
		ClassLoader loader = this.compile(filePath(testedClassName));
		Class<?> bindingClass = loader.loadClass(testedClassName + "Binding");
		Class<?> testedClass = loader.loadClass(testedClassName);

		assertChildBindings(bindingClass, "foo", "foofoo", "hashCodeBinding", "toStringBinding");
		Object testObj = testedClass.newInstance();
		Binding<Object> bindingObj = (Binding<Object>) bindingClass.getConstructor(testedClass).newInstance(testObj);
		Object fooBinding = bindingClass.getMethod("foo").invoke(bindingObj);
		Object foofooBinding = bindingClass.getMethod("foofoo").invoke(bindingObj);

		testedClass.getMethod("foo", Integer.class).invoke(testObj, 5);
		assertEquals(testObj, testedClass.getMethod("foofoo", Integer.class).invoke(testObj, 55));

		assertEquals(Integer.valueOf(5), Binding.class.getMethod("get").invoke(fooBinding));
		assertEquals(Integer.valueOf(55), Binding.class.getMethod("get").invoke(foofooBinding));

		Binding.class.getMethod("set", Object.class).invoke(fooBinding, 7);
		Binding.class.getMethod("set", Object.class).invoke(foofooBinding, 77);

		assertEquals(Integer.valueOf(7), Binding.class.getMethod("get").invoke(fooBinding));
		assertEquals(Integer.valueOf(77), Binding.class.getMethod("get").invoke(foofooBinding));

	}
}
