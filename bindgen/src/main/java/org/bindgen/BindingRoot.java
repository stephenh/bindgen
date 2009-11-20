package org.bindgen;

/**
 * Interface for a property (field or method) binding.
 * 
 * @param R the type of the root object of the binding
 * @param T the type of the leaf object of the binding
 */
public interface BindingRoot<R, T> extends Binding<T> {

	/**
	 * @param root the explicit root to use for traversing the path
	 * @return the value for this binding when evaluated again <code>root</code> 
	 */
	T getWithRoot(R root);

	/**
	 * @param root the explicit root to use for traversing the path
	 * @param value the new value for this binding
	 */
	void setWithRoot(R root, T value);

}
