package org.bindgen.example;

import static bindgen.BindKeyword.bind;
import junit.framework.Assert;
import junit.framework.TestCase;

import org.bindgen.example.SimpleBean;

public class BindKeywordTest extends TestCase {

	public void testWithFieldExample() {
		SimpleBean e = new SimpleBean("name");
		Assert.assertEquals("name", bind(e).name().get());
	}

}
