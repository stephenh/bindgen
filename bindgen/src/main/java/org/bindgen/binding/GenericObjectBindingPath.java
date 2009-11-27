package org.bindgen.binding;

import java.util.Collections;
import java.util.List;

import org.bindgen.Binding;

/**
 * A binding that represents a generic object.
 * 
 * This binding is usually used when there is no type-specific binding
 * because, for example, the type is outside of bindgen's scope.
 *
 * Note this is still abstract--the {@code getType}, {@code getName},
 * and {@code getWithRoot} methods will be defined by the {@code MyXxx}
 * concrete class.
 *
 * @author igor.vaynberg
 *
 * @param <R> type of root object
 */
public abstract class GenericObjectBindingPath<R, T> extends AbstractBinding<R, T> {

	private static final long serialVersionUID = 1L;

	@Override
	public List<Binding<?>> getChildBindings() {
		return Collections.emptyList();
	}

}
