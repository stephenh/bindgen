---
layout: default
title: Setup
---

Setup
=====

Bindgen is an annotation processor that you configure the Java compiler to run during its compilation run.

Ivy/Maven Repository
--------------------

You can download Bindgen releases from the [Joist repo](http://repo.joist.ws). It has both Ivy `ixy.xml` and Maven `pom.xml` artifacts, e.g.:

[http://repo.joist.ws/org/bindgen/bindgen/](http://repo.joist.ws/org/bindgen/bindgen/)

You will only need `bindgen.jar` on your classpath, but you can download the sources/etc. as well.

`javac`
-------

If you're using Ant, or just `javac` in general, you can use something like:

<pre name="code" class="xml">
    &lt;javac destdir="bin/main" source="1.6" target="1.6"&gt;
      &lt;classpath refid="main.classpath"/&gt;
      &lt;src path="src/main/java"/&gt;
      &lt;compilerarg value="-s"/&gt;
      &lt;compilerarg value="bin/apt"/&gt;
      &lt;compilerarg value="-processor"/&gt;
      &lt;compilerarg value="org.bindgen.processor.Processor"/&gt;
    &lt;/javac&gt;
</pre>

Briefly:

* `source="1.6"` and `target="1.6"` are required to kick in JDK6 APT support
* `main.classpath` is a classpath that includes `bindgen.jar`
* `-s bin/apt` configures where all APT-generated code is output
* `-processor org.bindgen.processor.Processor` tells `javac` the class name of the Bindgen processor to run

Eclipse
-------

The Eclipse annotation processing is configured with Project Properties:

For `Project Properties / Java Compiler`:

* Ensure the "Compiler compliance level" is 1.6

For `Project Properties / Java Compiler / Annotation Processing`:

* Check "Enable annotation processing"
* Check "Enable processing in editor"
* Optionally set "Generated source directory" to something like "bin/apt"

For `Project Properties / Java Compiler / Annotation Processing / Factory Path`:

* Click "Add JARs" and select the `bindgen.jar`


