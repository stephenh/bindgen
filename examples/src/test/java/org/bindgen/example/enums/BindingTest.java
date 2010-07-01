package org.bindgen.examples.enums;

import junit.framework.TestCase;

public class BindingTest extends TestCase {
    public void testEnum() {
        new FunEnumBinding(FunEnum.FIRST).funLevel().get();
    }
}
