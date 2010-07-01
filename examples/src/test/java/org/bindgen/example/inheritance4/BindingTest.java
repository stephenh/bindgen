package org.bindgen.example.inheritance4;

import junit.framework.Assert;
import junit.framework.TestCase;

public class BindingTest extends TestCase {

	public void testInheritedBinding() {
		ChildBinding b = new ChildBinding(new ChildImpl());
		b.childField().set("string1");
		b.parentField().set("string2");
		Assert.assertEquals("string1", b.childField().get());
		Assert.assertEquals("string2", b.parentField().get());
	}

	private static class ChildImpl implements Child {
		private String parent;
		private String child;

		@Override
		public String getParentField() {
			return this.parent;
		}

		@Override
		public void setParentField(String field) {
			this.parent = field;
		}

		@Override
		public String getChildField() {
			return this.child;
		}

		@Override
		public void setChildField(String field) {
			this.child = field;
		}
	}
}
