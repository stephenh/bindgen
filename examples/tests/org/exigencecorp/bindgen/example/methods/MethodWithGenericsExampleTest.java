package org.exigencecorp.bindgen.example.methods;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import bindgen.org.exigencecorp.bindgen.example.methods.MethodWithGenericsExampleBinding;

public class MethodWithGenericsExampleTest extends TestCase {

    public void testReadWrite() {
        MethodWithGenericsExample e = new MethodWithGenericsExample();
        MethodWithGenericsExampleBinding b = new MethodWithGenericsExampleBinding(e);

        List<String> originalList = e.getList();
        Assert.assertSame(originalList, b.list().get());

        b.list().set(new ArrayList<String>());
        Assert.assertNotSame(originalList, b.list().get());
    }

}
