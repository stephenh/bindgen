package org.exigencecorp.bindgen.example;

import junit.framework.Assert;
import junit.framework.TestCase;

public class MethodBindingExampleTest extends TestCase {

    public void testReadWrite() {
        MethodBindingExample e = new MethodBindingExample("1", "name");
        MethodBindingExampleBinding b = new MethodBindingExampleBinding(e);

        Assert.assertEquals("name", b.name().get());

        b.name().set("name1");
        Assert.assertEquals("name1", e.getName());
    }

    public void testReadOnly() {
        MethodBindingExample e = new MethodBindingExample("1", "name");
        MethodBindingExampleBinding b = new MethodBindingExampleBinding(e);

        Assert.assertEquals("1", b.id().get());

        try {
            b.id().set("name1");
            Assert.fail();
        } catch (RuntimeException re) {
            Assert.assertEquals("id is read only", re.getMessage());
        }
    }

}
