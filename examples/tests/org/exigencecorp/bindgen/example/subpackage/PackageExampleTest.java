package org.exigencecorp.bindgen.example.subpackage;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.bindgen.example.subpackage.PackageExample;

import bindgen.org.bindgen.example.subpackage.PackageExampleBinding;

public class PackageExampleTest extends TestCase {

	public void testReadWrite() {
		PackageExample e = new PackageExample("name");
		PackageExampleBinding b = new PackageExampleBinding(e);

		Assert.assertEquals("name", b.name().get());

		b.name().set("name1");
		Assert.assertEquals("name1", e.name);
	}

}
