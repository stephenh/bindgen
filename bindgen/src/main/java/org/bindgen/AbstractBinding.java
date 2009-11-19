package org.bindgen;

import java.util.ArrayList;
import java.util.List;

/**
 * A base implementation of {@link BindingRoot} to hold the starting
 * <code>T</code> value for evaluating bindings paths.
 */
public abstract class AbstractBinding<R, T> implements BindingRoot<R, T> {

	protected T _value;

	@Override
	public T get() {
		return this._value;
	}

	@Override
	public void set(T value) {
		this._value = value;
	}

	@Override
	public void setWithRoot(R root, T value) {
		throw new RuntimeException("Should be overridden by a field/method-specific binding.");
	}

	@Override
	public List<Binding<?>> getChildBindings() {
		return new ArrayList<Binding<?>>();
	}

	@Override
	public Binding<?> getParentBinding() {
		return null;
	}

	@Override
	public String toString() {
		if (this.getParentBinding() == null) {
			return this.getClass().getSimpleName() + "(" + this.get() + ")";
		} else {
			return this.getParentBinding().toString() + "." + this.getName();
		}
	}

	@Override
	public String getPath() {
		if (this.getParentBinding() == null) {
			return "#root";
		} else if (this.getParentBinding().getParentBinding() == null) {
			return this.getName();
		} else {
			return this.getParentBinding().getPath() + "." + this.getName();
		}
	}
}
