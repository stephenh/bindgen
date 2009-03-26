package org.exigencecorp.bindgen.example.inheritance;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.exigencecorp.bindgen.Bindable;
import org.exigencecorp.bindgen.Binding;

import bindgen.org.exigencecorp.bindgen.example.inheritance.SubExampleTestBinding;

@Bindable
public class SubExampleTest extends TestCase {

    public SubExample sub;

    public void testFoo() {
        Assert.assertEquals(null, this.sub);
        SubExample sub = new SubExample();

        SubExampleTestBinding b = new SubExampleTestBinding(this);
        Binding<? super SubExample> bind = b.sub();
        bind.set(sub);
        Assert.assertSame(sub, this.sub);

        b.sub().set(sub);
        Assert.assertSame(sub, this.sub);
    }

}
