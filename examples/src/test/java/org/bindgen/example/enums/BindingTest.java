package org.bindgen.example.enums;

import junit.framework.TestCase;

public class BindingTest extends TestCase {
	public void testEnum() {
		new FunEnumBinding(FunEnum.FIRST).funLevel().get();
	}
}
