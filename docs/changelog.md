---
layout: default
title: Changelog
---

Changelog
=========

## 2.5 - 2009-11-22

* Changed: Binding extends `Serializable`
* Changed: `GenericObjectBindingPath` is parameterized on its bound type `T`

## 2.4 - 2009-11-19

* Changed: Bindings are now generated in the same package as their target class, except for `java.*` (Igor Vaynberg)
* Changed: Bindings now include bindings for their target class's package private and protected members (Igor Vaynberg)
* Changed: the `skipBindgenKeyword` setting is now `skipBindKeyword`
* New: a package-based [scope](config.html) setting to reduce Bindgen's recursive sprawl (Igor Vaynberg)
* New: Settings are automatically loaded from an optional `bindgen.properties` in the project's root directory
* New: `javac` is automatically detected so `skipExistingBindingCheck` does not need to be explicitly set anymore
* New: `Binding.getPath()` method returns an OGNL-like representation of the binding
* New: `Binding.toString()` implementation returns a pretty representation of the binding
* New: `Binding.getIsSafe()` method to detect whether `get`/`set` calls will cause `NPEs`
* New: `Binding.getSafely()` method to return `null` instead of `NPE` if a parent value is not set
* New: [skipGeneratedTimestamps](config.html) setting
* New: experimental `bindgen.gwt.xml` for using Bindgen in GWT

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

