---
layout: default
title: Examples
---

Examples
========

Given a class `Foo`, annotated with `@Bindable`, Bindgen automatically generates `FooBinding` during the compile phase of `javac` or Eclipse.

`FooBinding` instances can then be constructed around an instance of `foo` and provide type-safe `Binding`s for `foo`'s properties.

Foo Example
-----------

For example, with class `Foo`:

<pre name="code" class="java">
    @Bindable
    public class Foo {
      public String name;
      public Bar bar;
    }

    public class Bar {
      public String zaz;
    }
</pre>

During compilation, the compiler will have Bindgen generate a class `FooBinding` that looks basically like:

<pre name="code" class="java">
    public class FooBinding {
      private Foo foo;

      // for turning Foo.name into a Binding
      public StringBinding name() {
        return new StringBinding {
          public String get() {
            return FooBinding.this.foo.name;
          }
          public void set(String name) {
            FooBinding.this.foo.name = name;
          }
        };
      }

      // for turning Foo.bar into a Binding
      public BarBinding bar() {
        return new BarBinding {
          public Bar get() {
            return FooBinding.this.foo.bar;
          }
          public void set(Bar bar) {
            FooBinding.this.foo.bar = bar;
          }
        };
      }
    }

    public class BarBinding {
      private Bar bar;

      // for turning Bar.zaz into a Binding
      public StringBinding zaz() {
        return new StringBinding() {
          public String get() {
            return BarBinding.this.bar.zaz;
          }
          public void set(String zaz) {
            BarBinding.this.bar.zaz = zaz;
          }
        };
      }
    }
</pre>

Note that Bindgen recursively generated a `BarBinding` class--this is assuming `Bar` is within the configured `scope` (see [config](config.html)).

You can now use the bindings like:

<pre name="code" class="java">
    Foo foo = new Foo();
    FooBinding fooBinding = new FooBinding(foo);

    // equivalent to foo.name "bob";
    StringBinding nameBinding = fooBinding.name();
    nameBinding.set("bob");

    // equivalent to foo.getBar().zaz = "zaz";
    StringBinding zazBinding = fooBinding.bar().zaz();
    zazBinding.set("zaz");
</pre>

Of course, it does not make sense to call `fooBinding.bar().zaz().set("zaz")` directly--bindings are typically most useful when frameworks understand them.

Framework Example
-----------------

The benefit of `fooBinding.bar().zaz()` over `foo.getBar().setZaz()` is that we can pass `zazBinding` around like an UL/OGNL expression for frameworks to get/put data in/out of.

The [joist](http://joist.ws/web.html) web framework is what drove Bindgen's development, but the same idea should apply to other frameworks as well. Some pseudo-code:

<pre name="code" class="java">
    @Bindable
    public class HomePage extends AbstractPage {

      public Form form = new Form("Login");
      // assigned by the framework
      public Employee employee = null;

      @Override
      public void onInit() {
        // static import of BindKeyword.bind
        HomePageBinding page = bind(this);

        // read on render/set on post the employee's name
        form.add(new TextField(page.employee().name()));

        // read on render/set on post the employer's name
        form.add(new TextField(page.employee().employer().name()));

        // call our submit method on POST
        form.add(new SubmitField(page.submit()));
      }

      public void submit() {
         // do stuff with updated this.employee
      }
    }
</pre>

The key point here is that in 1 line we can pass to a framework the ability to `get` and `set` a property of our domain objects.

And we've done so in a type-safe manner that is compile-time checked and will break if our domain objects change.

Stateless Example
-----------------

Bindings can also be stateless. This means you do not have to re-instantiate bindings every time you want to evaluate them, even if you have a different "root" instance to evaluate the binding against.

For example:

<pre name="code" class="java">
    // Store this in a map/static variable
    StringBindingPath&lt;Foo&gt; nameBinding = new FooBinding().name();

    // Later call getWithRoot(Foo) instead of just get(), e.g.:

    // thread1
    Foo f1 = new Foo("name1");
    nameBinding.getWithRoot(f1);
    nameBinding.setWithRoot(f1, "name11");

    // thread2
    Foo f2 = new Foo("name2");
    nameBinding.getWithRoot(f2);
    nameBinding.setWithRoot(f2, "name22");
</pre>

By using the `getWithRoot`/`setWithRoot` methods, two threads can safely share a single `StringBinding` instance and not worry about stepping on each other's toes as one evaluates the binding against `f1` and the other evaluates it against `f2`.

