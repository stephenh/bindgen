package org.bindgen.processor.generators;

import junit.framework.Assert;

import org.junit.Test;

public class AccessorPrefixTest {

	@Test
	public void testMatches() {
		Assert.assertEquals(true, AccessorPrefix.GET.matches("getFoo"));
		Assert.assertEquals(false, AccessorPrefix.GET.matches("getfoo"));
		Assert.assertEquals(false, AccessorPrefix.GET.matches("foo"));

		Assert.assertEquals(true, AccessorPrefix.NONE.matches("foo"));
		Assert.assertEquals(false, AccessorPrefix.NONE.matches("getFoo"));
		Assert.assertEquals(false, AccessorPrefix.NONE.matches("isFoo"));
		Assert.assertEquals(false, AccessorPrefix.NONE.matches("hasFoo"));
	}

}
