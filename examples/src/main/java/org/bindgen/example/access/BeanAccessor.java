package org.bindgen.example.access;

import org.bindgen.Bindable;
import org.bindgen.Binding;

@Bindable
public class BeanAccessor {
	public Bean bean;

	public Binding<?> getPublicBinding() {
		return new BeanAccessorBinding(this).bean().publicField();
	}

	public Binding<?> getProtectedBinding() {
		return new BeanAccessorBinding(this).bean().protectedField();
	}

	public Binding<?> getPackageBinding() {
		return new BeanAccessorBinding(this).bean().packageField();
	}

}
