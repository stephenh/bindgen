package org.bindgen.outofscope;

import org.bindgen.Bindable;

/**
 * A bean that is out of bindgen's scope and so no binding should be generated.
 */
@Bindable
public class GenericOut<T> {
	public T value;
}
