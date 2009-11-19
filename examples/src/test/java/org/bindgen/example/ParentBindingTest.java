package org.bindgen.example;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.bindgen.Binding;
import org.bindgen.Bindings;
import org.bindgen.example.Parents.Foo;
import org.bindgen.example.Parents.FooChild;
import org.bindgen.example.Parents.FooPage;
import org.bindgen.example.Parents.Zaz;
import org.bindgen.example.parents.FooBinding;
import org.bindgen.example.parents.FooChildBinding;
import org.bindgen.example.parents.FooPageBinding;
import org.bindgen.example.parents.ZazBinding;

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

	public void testSameBindingIfSameInstance() {
		Foo f1 = new Foo();
		// no f2
		FooBinding b1 = new FooBinding(f1);
		FooBinding b2 = new FooBinding(f1);
		Assert.assertNotSame(b1.bar(), b2.bar());
		Assert.assertEquals(true, this.areForSameProperty(b1.bar(), b2.bar()));
	}

	public void testNotSameBindingIfDifferentInstances() {
		Foo f1 = new Foo();
		Foo f2 = new Foo();
		FooBinding b1 = new FooBinding(f1);
		FooBinding b2 = new FooBinding(f2);
		Assert.assertNotSame(b1.bar(), b2.bar());
		Assert.assertEquals(false, this.areForSameProperty(b1.bar(), b2.bar()));
	}

	public void testSameBindingIfSameInstanceOverMultipleLevels() {
		Foo f1 = new Foo();
		f1.bar = "string";
		// no f2
		FooBinding b1 = new FooBinding(f1);
		FooBinding b2 = new FooBinding(f1);
		Assert.assertNotSame(b1.bar().empty(), b2.bar().empty());
		Assert.assertEquals(true, this.areForSameProperty(b1.bar().empty(), b2.bar().empty()));
	}

	public void testSameBindingIfSameIntermediateInstanceOverMultipleLevels_FailsTheHeuristic() {
		Foo f1 = new Foo();
		f1.bar = "string";
		Foo f2 = new Foo();
		f2.bar = f1.bar;
		FooBinding b1 = new FooBinding(f1);
		FooBinding b2 = new FooBinding(f2);
		Assert.assertNotSame(b1.bar().empty(), b2.bar().empty());
		// Assert.assertEquals(false, this.areForSameProperty(b1.bar().empty(), b2.bar().empty()));
		// This is a false positive due to our 1-look-back heuristic
		Assert.assertEquals(true, this.areForSameProperty(b1.bar().empty(), b2.bar().empty()));
	}

	public void testSameBindingIfSameInstanceViaDifferentPaths() {
		Foo f1 = new Foo();
		f1.bar = "string";
		FooChild fc = new FooChild();
		fc.foo = f1;
		FooBinding b1 = new FooBinding(f1);
		FooChildBinding b2 = new FooChildBinding(fc);
		Assert.assertNotSame(b1.bar(), b2.foo().bar());
		Assert.assertEquals(true, this.areForSameProperty(b1.bar(), b2.foo().bar()));
	}

	public void testNotSameBindingIfDifferentInstanceViaDifferentPaths() {
		Foo f1 = new Foo();
		f1.bar = "string";
		Foo f2 = new Foo();
		f2.bar = "f2";
		FooChild fc = new FooChild();
		fc.foo = f2;
		FooBinding b1 = new FooBinding(f1);
		FooChildBinding b2 = new FooChildBinding(fc);
		Assert.assertNotSame(b1.bar(), b2.foo().bar());
		Assert.assertEquals(false, this.areForSameProperty(b1.bar(), b2.foo().bar()));
	}

	public void testSameBindingIfSameInstanceViaDifferentPathsOverMultipleLevels() {
		Foo f1 = new Foo();
		f1.bar = "string";
		FooChild fc1 = new FooChild();
		fc1.foo = f1;
		FooPage fp1 = new FooPage();
		fp1.foo = f1;
		FooChildBinding b1 = new FooChildBinding(fc1);
		FooPageBinding b2 = new FooPageBinding(fp1);
		Assert.assertNotSame(b1.foo().bar(), b2.foo().bar());
		Assert.assertEquals(true, this.areForSameProperty(b1.foo().bar(), b2.foo().bar()));
	}

	public void testTheSameBindingIfSameIntermediatryInstanceButDifferentRootAndSameExactPath() {
		Foo f1 = new Foo();
		f1.bar = "string";
		FooChild fc1 = new FooChild();
		fc1.foo = f1;
		FooChild fc2 = new FooChild();
		fc2.foo = f1;
		FooChildBinding b1 = new FooChildBinding(fc1);
		FooChildBinding b2 = new FooChildBinding(fc2);
		Assert.assertNotSame(b1.foo().bar(), b2.foo().bar());
		// Should probably say true because Foo is not a value-object--how can we detect value vs. non-value objects?
		Assert.assertEquals(true, this.areForSameProperty(b1.foo().bar(), b2.foo().bar()));
	}

	public void testNotSameBindingIfSameValueObjectOnDifferentPaths() {
		Foo f1 = new Foo();
		f1.bar = "string";
		Zaz z1 = new Zaz();
		z1.name = "string"; // not even the say property name

		FooBinding b1 = new FooBinding(f1);
		ZazBinding b2 = new ZazBinding(z1);
		Assert.assertEquals(false, this.areForSameProperty(b1.bar(), b2.name()));
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

	private boolean areForSameProperty(Binding<?> b1, Binding<?> b2) {
		return Bindings.areForSameProperty(b1, b2);
	}
}
