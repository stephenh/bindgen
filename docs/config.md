---
layout: default
title: Configuration
---

Configuration
=============

Bindgen has several configurable parameters that are documented here.

bindgen.properties
------------------

Bindgen's configuration is stored in a `bindgen.properties` file at the root of your project.

Technically, the APT API provides a way to pass key/value pairs to processors via compiler arguments (e.g. `-Akey=value`), but this proves less than ideal when you have more than a few properties and also multiple tools compiling your code (e.g. having to configure the `build.xml`, Eclipse `.settings`, etc.)

So you can use `-Akey=value` arguments, but a separate `bindgen.properties` file is recommended.

Scope
-----

Given a class annotated with `@Bindable`, Bindgen by default recurses into that class's fields/methods and generates bindings for their types, and their types fields'/methods' types, etc. This is what allows you to chain together a binding like `b.foo().bar().name()`.

However, if unchecked, this recursion can result in a lot of sprawl, especially if Bindgen gets into a framework like Spring or Hibernate.

To limit the recursion to not escape certain package boundaries, you can set the `scope` property:

<pre name="code">
    scope=com.myapp,java
</pre>

Block Types to Attempt
----------------------

Besides fields/getters/setters, Bindgen can generate callable bindings.

Out of the box, Bindgen will wrap `void`, no-argument methods like `void foo()` into a `Runnable foo()` that invokes the original `foo` when the Runnable `run()` is called.

To customize the pseudo-closure like types Bindgen inspects your methods for, set `blockTypes`, e.g.:

<pre name="code">
    blockTypes=com.myapp.Block
</pre>

The `com.myapp.Block` type should have on method. Note that it is allowed to have parameters.

Skip Attribute
--------------

Sometimes you just don't want a property to have a binding generated for it. In this case, set:

    skipAttribute.com.myapp.FooClass.barProperty=true

Skip Bind Keyword
-----------------

By default, Bindgen creates a `org.bindgen.BindKeyword` class with a static `bind` method. This `bind` method is overloaded for each class that had bindings generated, so that you can use `bind(this)` and get back a type-safe `Binding` instance.

If you do not want this behavior, set:

    skipBindKeyword=true

Skip Generated Timestamps
-------------------------

By default, Bindgen puts a `@Generated` annotation in the source output of any bindings it generates. This identifies Bindgen as the originator of the source and also the timestamp of when it was created.

If you don't want the `@Generated` put, you can set the property:

    skipGeneratedTimestamps=true

Fix Raw Types
-------------

Occasionally an old API will use generic classes but without the generics. This can cause a headache to bind against, so you can override the missing generic.

For example, this option:

    fixRawType.javax.servlet.ServletRequest.parameterMap=String, String[]

Changes the binding type for the pre-1.5 `ServletRequest.getParameterMap` method from a raw `Map` to a `Map<String, String[]`>

Note that Bindgen by default includes `fixRawType.` settings for any servlet API methods that use raw types.

Binding Super Class
-------------------

The generated `XxxBindingPath` classes typically extend an internal Bindgen `AbstractBinding` class. If you'd prefer to have them extend your own class, you can set:

    bindingPathSuperClass=your.class.Name


