package org.exigencecorp.bindgen.example;

import junit.framework.Assert;
import junit.framework.TestCase;

public class PropertyBindingExampleTest extends TestCase {

    public void testEmployee() {
        PropertyBindingExample e = new PropertyBindingExample();
        e.setName("entity");

        PropertyBindingExampleBinding b = new PropertyBindingExampleBinding();
        b.set(e);
        Assert.assertEquals("entity", b.name().get());
        b.name().set("entity1");
        Assert.assertEquals("entity1", e.getName());
    }

}
