package org.exigencecorp.bindgen.example.methods;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.exigencecorp.bindgen.Binding;

import bindgen.org.exigencecorp.bindgen.example.methods.MethodExampleBinding;

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

    public void testBoolean() {
        MethodExample e = new MethodExample("1", "name");
        MethodExampleBinding b = new MethodExampleBinding(e);
        Assert.assertEquals(false, b.good().get().booleanValue());

        b.good().set(true);
        Assert.assertEquals(true, e.isGood());
    }

    public void testToString() {
        MethodExample e = new MethodExample("1", "name");
        MethodExampleBinding b = new MethodExampleBinding(e);
        Assert.assertEquals("method", b.string().get());
    }

    public void testHasMethod() {
        MethodExample e = new MethodExample("1", "name");
        MethodExampleBinding b = new MethodExampleBinding(e);
        Assert.assertEquals(false, b.stuff().get().booleanValue());
    }

    public void testBooleanThatIsAKeywordFallsBackOnMethodName() {
        MethodExample e = new MethodExample("1", "name");
        MethodExampleBinding b = new MethodExampleBinding(e);
        Assert.assertEquals(false, b.isNew().get().booleanValue());
    }

    public void testGetBindings() {
        MethodExample e = new MethodExample("1", "name");
        MethodExampleBinding b = new MethodExampleBinding(e);
        Assert.assertEquals(6, b.getBindings().size());

        boolean foundName = false;
        for (Binding<?> sub : b.getBindings()) {
            if (sub.getName().equals("name")) {
                foundName = true;
            }
        }
        Assert.assertTrue(foundName);
    }

}
