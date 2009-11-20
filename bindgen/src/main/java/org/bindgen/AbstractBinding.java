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
			// This is kind of lame, but GWT doesn't support getSimpleName, so use getName
			String className = this.getClass().getName();
			String simpleName = className.substring(className.lastIndexOf(".") + 1);
			return simpleName + "(" + this.get() + ")";
		} else {
			Object value = this.getIsSafe() ? this.get() : "";
			return this.getParentBinding().toString() + "." + this.getName() + "(" + value + ")";
		}
	}

	@Override
	public boolean getIsSafe() {
		if (this.getParentBinding() == null) {
			return true;
		} else if (this.getParentBinding().getIsSafe()) {
			return this.getParentBinding().get() != null;
		} else {
			return false;
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

	@Override
	public T getSafely() {
		if (this.getIsSafe()) {
			return this.get();
		} else {
			return null;
		}
	}

}
