---
layout: default
title: Bindgen
---

Bindgen
=======

Overview
--------

Bindgen is a type-safe alternative to expression languages like UL and OGNL. 

It provides:

* A succinct way to pass your domain object properties as get-able/set-able [`Binding`][binding] objects.
* Compile-time checking of binding expressions that will break if your domain model changes.

See [examples](examples.html) for the binding syntax.

Approach
--------

Bindgen uses code generation, but is implemented as a JDK6 annotation processor to provide (in Eclipse) a seamless editing/generation experience. The generated code is kept up to date as soon as "save" is hit.

When save is hit, Bindgen inspects the class that just changed and generates a mirror `XxxBinding` class that has type-safe methods that return `Binding` instances that wrap around each of your class's public properties (fields or methods).

Again, see the [examples](examples.html) for more details.

Sections
--------

* [Examples](examples.html)
* [Setup](setup.html)
* [Screencasts](screencasts.html)
* [Performance](performance.html)

[binding]: http://github.com/stephenh/bindgen/blob/master/bindgen/src/org/bindgen/Binding.java

