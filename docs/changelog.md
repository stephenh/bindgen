---
layout: default
title: Changelog
---

Changelog
=========

## master - unreleased

* Add package-based scope to reduce recursive sprawl (Igor Vaynberg)
* Generate bindings in the same package as their class, except for `java.*` (Igor Vaynberg)
* Load configuration from `bindgen.properties`
* Misc refactoring

## 2.3 - 2009-11-11

* Fix raw Lists/Sets breaking the `getContainedType`
* Compile example source and tests separately
* Avoid NPE in BindKeywordGenerator
* Add maven build for examples

## 2.2 - 2009-11-10

* Fix bindings for wildcards that were not the first type parameter
* Upgrade to `joist-util-0.3`, include the source zip, and skip IvyDE for now
* Rename `org.exigencecorp.bindgen` -> `org.bindgen`, `dos2unix`, spaces to tabs

## 2.1 - 2009-11-09

* Added `null` as a keyword to avoid compilation errors for methods like `isNull`
* Added error logging to `SOURCE_OUTPUT/bindgen-exception.txt`
* Fix for service locator build error (Igor Vaynberg)
* Flushed out javadocs in the Bindgen source code
* Officially license as Apache

## 2.0 - 2009-07-21

* Added stateless bindings
* Support for Array bindings
* Large cleanup/refactoring of bindgen source code

## 1.0 - 2009-03-26

* Initial release

