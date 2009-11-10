package org.bindgen;

import java.util.List;

/**
 * Interface for a property (field or method) binding.
 *
 * @param T the type of the leaf object of the binding
 */
public interface Binding<T> extends NamedBinding {

	/** @return the value for this binding */
	T get();

	/** @param value the new value for this binding */
	void set(T value);

	/** @return the type <code>T</code> for this binding */
	Class<?> getType();

	/** @return the parent binding, e.g. parent if we are foo in binding.parent().foo() */
	Binding<?> getParentBinding();

	/** @return the bindings of the attributes for our current instance. */
	List<Binding<?>> getChildBindings();

}
