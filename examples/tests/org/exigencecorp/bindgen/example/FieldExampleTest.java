package org.exigencecorp.bindgen.example;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import bindgen.org.exigencecorp.bindgen.example.FieldExampleBinding;

public class FieldExampleTest extends TestCase {

    public void testReadWrite() {
        FieldExample e = new FieldExample("name");
        FieldExampleBinding b = new FieldExampleBinding(e);

        Assert.assertEquals("name", b.name().get());

        b.name().set("name1");
        Assert.assertEquals("name1", e.name);
    }

    public void testListReadWrite() {
        FieldExample e = new FieldExample("name");
        FieldExampleBinding b = new FieldExampleBinding(e);

        List<String> list = b.list().get();
        list.add("foo");

        Assert.assertEquals("foo", e.list.get(0));
        Assert.assertSame(list, b.list().get());
    }

}
