package org.exigencecorp.bindgen.example.inheritance;

import junit.framework.Assert;
import junit.framework.TestCase;
import bindgen.org.exigencecorp.bindgen.example.inheritance.SubExampleBinding;

public class BaseExampleTest extends TestCase {

    public void testSubBindings() {
        SubExample sub = new SubExample();
        SubExampleBinding subb = new SubExampleBinding(sub);
        subb.name().set("foo");
        subb.subOnly().set("bar");

        Assert.assertEquals("foo", sub.name);
        Assert.assertEquals("bar", sub.subOnly);
        // 3 == base description, sub name, sub subOnly
        Assert.assertEquals(3, subb.getBindings().size());
    }

    public void testSubBindingsWithRealSub() {
        SubExampleBinding subb = new SubExampleBinding();
        subb.set(new SubExample());
        Assert.assertEquals(null, subb.subOnly().get());
    }

    public void testSubBindingsWithBase() {
        SubExampleBinding subb = new SubExampleBinding();
        subb.set(new BaseExample());
        try {
            subb.subOnly().get();
            Assert.fail();
        } catch (NullPointerException npe) {
            // Weird--but it should not work, just expected a different exception, like ClassCastException
        }
    }

}
