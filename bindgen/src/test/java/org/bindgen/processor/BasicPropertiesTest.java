package org.bindgen.processor;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.bindgen.Binding;
import org.junit.Test;

public class BasicPropertiesTest extends AbstractBindgenTestCase {
	private static final String NAME = "John Doe";
	private static final String CITY = "Beverly Hills";
	private static final String PACKAGE = "org.bindgen.processor.basic.";

	private static String path(String className) {
		return PACKAGE.replace(".", "/") + className + ".java";
	}

	private static String name(String className) {
		return PACKAGE + className;
	}

	@Test
	public void shouldCreateDirectBindings() throws Exception {
		ClassLoader loader = this.compile(path("Address"));

		Class<?> addressClass = loader.loadClass(name("Address"));
		Class<?> addressBindingClass = loader.loadClass(name("AddressBinding"));

		Object address = addressClass.newInstance();

		addressClass.getField("city").set(address, CITY);

		//test new AddressBinding(address).city().get()

		Object binding = addressBindingClass.getConstructor(addressClass).newInstance(address);
		Object cityBinding = addressBindingClass.getMethod("city").invoke(binding);
		String city = (String) Binding.class.getMethod("get").invoke(cityBinding);

		assertThat(CITY, is(city));
	}

	@Test
	public void shouldCreateRecursiveBindings() throws Exception {
		ClassLoader loader = this.compile(path("Address"), path("Person"));

		Class<?> addressClass = loader.loadClass(name("Address"));
		Class<?> addressBindingPathClass = loader.loadClass(name("AddressBindingPath"));
		@SuppressWarnings("unused")
		Class<?> addressBindingClass = loader.loadClass(name("AddressBinding"));

		Class<?> personClass = loader.loadClass(name("Person"));

		Class<?> personBindingClass = loader.loadClass(name("PersonBinding"));

		Object address = addressClass.newInstance();
		addressClass.getField("city").set(address, CITY);

		Object person = personClass.newInstance();
		personClass.getField("name").set(person, NAME);
		personClass.getField("address").set(person, address);

		// test new PersonBinding(person).address().city().get()

		Object binding = personBindingClass.getConstructor(personClass).newInstance(person);
		Object addressBinding = personBindingClass.getMethod("address").invoke(binding);
		Object cityBinding = addressBindingPathClass.getMethod("city").invoke(addressBinding);
		String city = (String) Binding.class.getMethod("get").invoke(cityBinding);

		assertThat(CITY, is(city));
	}

}
