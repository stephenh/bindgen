package org.bindgen.example.interfaceinner;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.bindgen.example.interfaceinner.Outer.Inner;
import org.bindgen.example.interfaceinner.outer.InnerBinding;

public class BindingTest extends TestCase {
	public void testInnerBinding() {
		InnerBinding b = new InnerBinding(new Inner());
		b.something().set("string1");
		Assert.assertEquals("string1", b.something().get());
	}
}
