package org.exigencecorp.bindgen.example;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.exigencecorp.bindgen.example.EnumExample.Foo;

import bindgen.org.exigencecorp.bindgen.example.EnumExampleBinding;

public class EnumExampleTest extends TestCase {

    public void testEnum() {
        EnumExample e = new EnumExample();
        EnumExampleBinding b = new EnumExampleBinding(e);
        b.foo().set(Foo.ONE);
        Assert.assertEquals(Foo.ONE, e.foo);
    }

}
