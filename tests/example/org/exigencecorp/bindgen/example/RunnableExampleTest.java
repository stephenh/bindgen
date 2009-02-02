package org.exigencecorp.bindgen.example;

import junit.framework.Assert;
import junit.framework.TestCase;
import bindgen.org.exigencecorp.bindgen.example.RunnableExampleBinding;

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

}
