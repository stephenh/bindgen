package org.exigencecorp.bindgen;

import java.util.List;

/**
 * Interface for a property (field or method) binding.
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

    /**
     * @param root the explicit root to use for traversing the path
     * @return the value for this binding when evaluated again <code>root</code> 
     */
    T getWithRoot(Object root);

    /**
     * @param root the explicit root to use for traversing the path
     * @param value the new value for this binding
     */
    void setWithRoot(Object root, T value);

}
