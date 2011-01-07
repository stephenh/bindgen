package org.bindgen.inscope;

import junit.framework.TestCase;

public class PersonTest extends TestCase {

	public void testExists() {
		assertNotNull(this.newInstance(AddressInBinding.class));
		assertNotNull(this.newInstance(HouseInBinding.class));
		assertNotNull(this.newInstance(CarInBinding.class));
	}

	private Object newInstance(Class<?> type) {
		try {
			return type.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
