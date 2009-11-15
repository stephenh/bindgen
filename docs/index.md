---
layout: default
title: Bindgen
---

Bindgen
=======

Overview
--------

[Bindgen](http://github.com/stephenh/bindgen) is a type-safe alternative to expression languages like UL and OGNL. 

Bindgen uses code generation, but is implemented as a JDK6 annotation processor to provide (in Eclipse) a seamless editing/generation experience. The generated code is kept up to date as soon as "save" is hit.

Approach
--------

Bindgen generates closure-like classes that mirror classes annotated with `@Bindable` and provides type-safe instances of the [`Binding`][binding] interface to allow frameworks to `get`/`set` properties' data.

Sections
--------

* [Examples](examples.html)
* [Screencasts](screencasts.html)
* [Performance](performance.html)
* [Setup](setup.html)

[binding]: http://github.com/stephenh/bindgen/blob/master/bindgen/src/org/bindgen/Binding.java

