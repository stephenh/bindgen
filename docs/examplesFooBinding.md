---
layout: default
title: Examples FooBinding
---

FooBinding
==========

The generated content of the `FooBinding` class is basically a lot of small inner-classes that defer evaluating their property until their `get` or `set` methods are called.

E.g. something like:

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

