package org.bindgen.binding;

import java.util.Collections;
import java.util.List;

import org.bindgen.AbstractBinding;
import org.bindgen.Binding;

/**
 * A binding that represents a generic object. This binding is usually used when there is no type-specific binding because, for example, the type is outside of bindgen's scope.
 * 
 * @author igor.vaynberg
 *
 * @param <R> type of root object
 */
public abstract class GenericObjectBindingPath<R> extends AbstractBinding<R, Object> {

	@Override
	public List<Binding<?>> getChildBindings() {
		return Collections.emptyList();
	}

}
