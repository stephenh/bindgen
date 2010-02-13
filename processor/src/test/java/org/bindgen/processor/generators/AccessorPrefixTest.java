package org.bindgen.processor.generators;

import junit.framework.Assert;

import org.junit.Test;

public class AccessorPrefixTest {

	@Test
	public void testGuess() {
		Assert.assertEquals(AccessorPrefix.GET, AccessorPrefix.guessPrefix("getFoo"));
		Assert.assertEquals(AccessorPrefix.IS, AccessorPrefix.guessPrefix("isFoo"));
		Assert.assertEquals(AccessorPrefix.HAS, AccessorPrefix.guessPrefix("hasFoo"));
		Assert.assertEquals(AccessorPrefix.NONE, AccessorPrefix.guessPrefix("foo"));
		Assert.assertEquals(AccessorPrefix.NONE, AccessorPrefix.guessPrefix("f"));
		Assert.assertEquals(AccessorPrefix.NONE, AccessorPrefix.guessPrefix("F"));
	}

}
