package org.bindgen.processor;

import org.junit.Test;

/** Tests without a scope to generate bindings for the java.* packages. */
public class NoScopeTest extends AbstractBindgenTestCase {

	@Test
	public void testPoint() throws Exception {
		this.setScope("");
		this.compile("org/bindgen/processor/basic/Point.java");
	}

	@Test
	public void testInnerEnum() throws Exception {
		this.setScope("");
		this.compile("org/bindgen/processor/inner/ClassWithEnum.java");
	}

}
