package org.bindgen.example.blocks;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.bindgen.NamedBinding;
import org.bindgen.example.blocks.RunnableExample;

import bindgen.org.bindgen.example.blocks.RunnableExampleBinding;

public class RunnableExampleTest extends TestCase {

	public void testRun() {
		RunnableExample e = new RunnableExample();
		Assert.assertFalse(e.isStuffDone());

		RunnableExampleBinding b = new RunnableExampleBinding(e);
		Runnable r = b.doStuff();
		Assert.assertFalse(e.isStuffDone());

		r.run();
		Assert.assertTrue(e.isStuffDone());
	}

	public void testRunName() {
		RunnableExample e = new RunnableExample();
		RunnableExampleBinding b = new RunnableExampleBinding(e);
		Runnable r = b.doStuff();
		Assert.assertEquals("doStuff", ((NamedBinding) r).getName());
	}

}
