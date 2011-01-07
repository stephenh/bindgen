package org.bindgen.processor.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class ClassNameTest {

	@Test
	public void stripRepeatedTypeVarsOfOuterClasses() {
		assertThat(//
			new ClassName("java.util.Map<K, V>.Entry<K, V>").get(),
			is("java.util.Map.Entry<K, V>"));
		assertThat(//
			new ClassName("java.util.Foo<K extends java.util.Bar<K>>.Entry<K extends java.util.Bar<K>>").get(),
			is("java.util.Foo.Entry<K extends java.util.Bar<K>>"));
	}

}
