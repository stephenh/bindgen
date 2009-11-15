---
layout: default
title: Building
---

Building
========

Note that this is for building Bindgen itself--to use Bindgen, you can just use a binary distribution as described in [setup](setup.html).

Overview
--------

Bindgen currently consists of two projects:

* `bindgen` which has the annotation processor implementation and
* `examples` which provides an integration-level test suite.

Building `bindgen` is supported via ant or Eclipse.

Building `examples` is supported via ant, Eclipse, or maven. Having `examples` also built via maven ensures `bindgen` works correctly in its environment.

Building `bindgen` in Ant
-------------------------

In the `bindgen/` directory, run `ant jar`. This will create a new `bin/jars/bindgen.jar` with Bindgen and its dependencies `jarjar`-ed together.

That's it--this one is fairly simple.

Building `bindgen` in Eclipse
-----------------------------

The `bindgen` Eclipse project leverages Eclipse RCP to run and debug a separate, child instance of Eclipse for the `examples` project.

This means you can debug the annotation processor as it is running over the `examples` project. This is very useful for development purposes and we extend huge thanks to [Walter Harley](http://www.cafewalter.com/), an Eclipse APT engineer, for introducing us to this method.

This does mean that you'll need the [Eclipse for RCP/Plug-in Developers](http://www.eclipse.org/downloads/) distribution of Eclipse instead of just the Java or Java EE distribution.

After you import `bindgen/.project` into an Eclipse RCP workspace, the combination of the `plugin.xml`, `META-INF/MANIFEST.MF`, the PluginNature in `.project`, and the `examples/lib/annotations.jar` (updated by running `ant annotations` when you change any Bindgen public API), means you should be able to launch the `BindgenExamples.launch` target.

This will create a new instance of Eclipse. Initially the workspace will be empty, so you will need to import the `examples/.project` file.

Now you should be set. Setting debug points in the Bindgen implementation and then either saving files or running clean in the `examples` Eclipse should hit your debug points.

Building `examples` in Ant
--------------------------

Run `ant tests`.

Note that this will use the latest `bindgen/bin/jars/bindgen.jar`, so you will need to run `ant jar` in the `bindgen` project first (and each time you want to see your Bindgen changes in the `examples` project).

Building `examples` in Eclipse
------------------------------

See the previous section on building `bindgen` in Eclipse--the RCP child Eclipse instance that runs against `examples` is the best way of working with the `examples` project in Eclipse.

Building `examples` in Maven
----------------------------

To test the snapshot version of Bindgen, first in `bindgen` run `ant ivy.publish-maven-user` to get `bindgen-SNAPSHOT` into your `~/.m2/repository`.

Now in `examples` run `mvn clean test`.

