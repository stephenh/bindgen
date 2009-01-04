package org.exigencecorp.bindgen.example;

import junit.framework.Assert;
import junit.framework.TestCase;

public class FieldExampleTest extends TestCase {

    public void testReadWrite() {
        FieldExample e = new FieldExample("name");
        FieldExampleBinding b = new FieldExampleBinding(e);

        Assert.assertEquals("name", b.name().get());

        b.name().set("name1");
        Assert.assertEquals("name1", e.name);
    }

}
