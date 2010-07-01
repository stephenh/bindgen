package org.bindgen.examples.interfaceinner;

import org.bindgen.examples.interfaceinner.outer.InnerBinding;

import junit.framework.TestCase;

public class BindingTest extends TestCase {
    public void testInnerBinding() {
        Object binding = new InnerBinding().something();
    }
}
