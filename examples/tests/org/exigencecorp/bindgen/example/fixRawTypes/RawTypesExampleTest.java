package org.exigencecorp.bindgen.example.fixRawTypes;

import java.util.Enumeration;
import java.util.Hashtable;

import junit.framework.Assert;
import junit.framework.TestCase;
import bindgen.org.exigencecorp.bindgen.example.fixRawTypes.RawTypesExampleBinding;

public class RawTypesExampleTest extends TestCase {

    private Hashtable<String, String> hash = new Hashtable<String, String>();
    private Enumeration<String> e = this.hash.keys();

    public void testField() {
        RawTypesExample r = new RawTypesExample();
        RawTypesExampleBinding b = new RawTypesExampleBinding(r);

        b.fieldGiven().set(this.e);
        Assert.assertSame(this.e, b.fieldGiven().get());

        b.fieldFixed().set(this.e);
        Assert.assertSame(this.e, b.fieldFixed().get());
    }

    public void testMethod() {
        RawTypesExample r = new RawTypesExample();
        RawTypesExampleBinding b = new RawTypesExampleBinding(r);

        b.methodGiven().set(this.e);
        Assert.assertSame(this.e, b.methodGiven().get());

        b.methodFixed().set(this.e);
        Assert.assertSame(this.e, b.methodFixed().get());
    }

}
