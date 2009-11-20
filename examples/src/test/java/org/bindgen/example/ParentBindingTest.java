package org.bindgen.example;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.bindgen.example.Parents.Foo;
import org.bindgen.example.Parents.FooChild;
import org.bindgen.example.parents.FooBinding;
import org.bindgen.example.parents.FooChildBinding;

public class ParentBindingTest extends TestCase {

	public void testParentBindingIsNullByDefault() {
		FooBinding b = new FooBinding();
		Assert.assertEquals(null, b.getParentBinding());
	}

	public void testParentBindingOfFieldProperty() {
		FooBinding b = new FooBinding();
		Assert.assertSame(b, b.bar().getParentBinding());
	}

	public void testParentBindingOfMethodProperty() {
		FooBinding b = new FooBinding();
		Assert.assertSame(b, b.baz().getParentBinding());
	}

	public void testToString() {
		FooChildBinding fcb = new FooChildBinding();
		Assert.assertEquals("FooChildBinding(null)", fcb.toString());
		Assert.assertEquals("FooChildBinding(null).foo()", fcb.foo().toString());
		Assert.assertEquals("FooChildBinding(null).foo().baz()", fcb.foo().baz().toString());

		// Now set FooChild
		fcb.set(new FooChild());
		Assert.assertEquals("FooChildBinding(child)", fcb.toString());
		Assert.assertEquals("FooChildBinding(child).foo(null)", fcb.foo().toString());
		Assert.assertEquals("FooChildBinding(child).foo(null).baz()", fcb.foo().baz().toString());

		// No set Foo
		fcb.get().foo = new Foo();
		Assert.assertEquals("FooChildBinding(child)", fcb.toString());
		Assert.assertEquals("FooChildBinding(child).foo(foo)", fcb.foo().toString());
		Assert.assertEquals("FooChildBinding(child).foo(foo).baz(baz)", fcb.foo().baz().toString());
	}

	public void testGetPath() {
		FooChildBinding fcb = new FooChildBinding();
		Assert.assertEquals("#root", fcb.getPath());
		Assert.assertEquals("foo", fcb.foo().getPath());
		Assert.assertEquals("foo.baz", fcb.foo().baz().getPath());
	}
}
