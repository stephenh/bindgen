---
layout: default
title: Changelog
---

Changelog
=========

## 2.18 - 2010-11-03

* New: `BindingRoot.getSafelyWithRoot` method
* Change: Deprecated fields/methods now have bindings generated for them (previously they were skipped to avoid warnings)
* Minor: Generated classes have `@SuppressWarnings("all")` to ensure no warnings

## 2.17 - 2010-10-27

* Fix: Array properties were read only (Thomas Matthijs)

## 2.16 - 2010-10-24

* Fix: Bindings of methods that had type variables bound in super interfaces were read only

## 2.15 - 2010-07-01

* Fix: Enums can now be annotated with `@Bindable` (David Shepherdson)
* Fix: Bindings of subclasses that fill in a parent's type parameters now compile (David Shepherdson)
* Fix: `@Bindable` classes inside of interfaces now compile (David Shepherdson)
* Fix: `Binding.getType` could cause unresolved type compile errors for inner classes
* Change: the dependencies in the pom are now either in the `test` or `system` scopes

## 2.14 - 2010-06-30

* Fix: Avoid clash on `Binding.getName` if `name` and `getName` methods both appeared in a class

## 2.13 - 2010-06-20

* Fix: Avoid NPE in Eclipse on `java.util.Map` and after type variable resolution introduced in versions 2.10/2.11
* Fix: Avoid NPE if user-configured `blockTypes` is invalid

## 2.12 - 2010-06-14

* Fix: Setter lookup failed for primitive types, resulted in their bindings being read only

## 2.11 - 2010-04-29

* Fix: Inherited bindings that used complex type variables in their signature where broken in javac

## 2.10 - 2010-03-24

* Fix: Inherited bindings that used type variables in their signature were broken in javac

## 2.9 - 2010-03-21

* New Feature: generate bindings for prefix-less getters (e.g. `int foo()`) (Mihai)
* New Feature: `bindingPathSuperClass` configuration property to have generated bindings extend a custom base class (Mihai) 
* Fix: Bindings are no longer non-deterministic/based on source-code order in cases where multiple properties overlap (Mihai)
* Fix: Correctly handle inner classes contained within outer classes that have a lower-case name
* Fix: Correctly handle bindings of classes in the default package
* Fix: Binding return types are the more generic `XxxBindingPath<R>` instead of the implementation-specific `MyXxxBinding` inner classes

## 2.8 - 2010-02-09

* Fix: Bindings inherited across package boundaries were ignored

## 2.7 - 2010-02-08

* Fix: Interface bindings include properties from extended interfaces

## 2.6 - 2009-11-26

* Fix: Protected bindings are now `set`-able
* Fix: Generated binding inner classes have `@Override` on `set` and `setWithRoot` methods
* Fix: Generated binding classes have a `serialVersionUID=1L` to avoid compiler warnings
* Changed: Moved `org/bindgen.gwt.xml` to `org/bindgen/Bindgen.gwt.xml`

## 2.5 - 2009-11-22

* Changed: Binding extends `Serializable` (Igor Vaynberg)
* Changed: `GenericObjectBindingPath` is parameterized on its bound type `T` (Igor Vaynberg)

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

