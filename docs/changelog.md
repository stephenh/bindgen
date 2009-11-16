---
layout: default
title: Changelog
---

Changelog
=========

## 2.3

* Fix raw Lists/Sets breaking the getContainedType
* Compile example tests and src separately
* Avoid NPE in BindKeywordGenerator
* Add maven2 build for examples

## 2.2

* Fix bindings for wildcards that were not the first type parameter
* Upgrade to `joist-util-0.3`, include the source zip, and skip IvyDE for now
* Rename `org.exigencecorp.bindgen` -> `org.bindgen`, `dos2unix`, spaces to tabs

## 2.1

* Added `null` as a keyword to avoid compilation errors for methods like `isNull`
* Added error logging to `SOURCE_OUTPUT/bindgen-exception.txt`
* Fix for service locator build error (from Igor Vaynberg <igor.vaynberg@gmail.com>)
* Flushed out javadocs in the bindgen source code 
* Officially license as Apache

## 2.0

* Added stateless bindings
* Support for Array bindings
* Large cleanup/refactoring of bindgen source code

