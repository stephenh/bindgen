package org.exigencecorp.bindgen.example;

import junit.framework.Assert;
import junit.framework.TestCase;
import bindgen.org.exigencecorp.bindgen.example.MethodExampleBinding;

public class MethodExampleTest extends TestCase {

    public void testReadWrite() {
        MethodExample e = new MethodExample("1", "name");
        MethodExampleBinding b = new MethodExampleBinding(e);

        Assert.assertEquals("name", b.name().get());

        b.name().set("name1");
        Assert.assertEquals("name1", e.getName());
    }

    public void testReadOnly() {
        MethodExample e = new MethodExample("1", "name");
        MethodExampleBinding b = new MethodExampleBinding(e);

        Assert.assertEquals("1", b.id().get());

        try {
            b.id().set("name1");
            Assert.fail();
        } catch (RuntimeException re) {
            Assert.assertEquals("id is read only", re.getMessage());
        }
    }

}
