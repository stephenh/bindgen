package org.bindgen.wicket;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.bindgen.BindingRoot;

/**
 * A model that access a property of an object using a binding path. The object is represented by an
 * IModel and the binding path is representated by {@link BindingRoot}. This model is the same as
 * {@link PropertyModel} but uses a binding instead of an unsafe string expression.
 * 
 * <p>
 * Example:
 * 
 * <pre>
 * add(new TextField("street1", new BindingRootModel&lt;Contact,String&gt;(person, new PersonBinding().address().street1()));
 * </pre>
 * 
 * or using the shorthand provided by {@link Bindings} class:
 * 
 * <pre>
 * import static org.bindgen.wicket.Bindings.*;
 * import static org.bindgen.BindKeyword.*;
 * 
 * add(new TextField("street1", model(person, new PersonBinding().address().street1())));
 * 
 * </p>
 * 
 * @author igor.vaynberg
 * 
 * @param <R>
 *            type of root object
 * @param <T>
 *            type of object returned by binding
 */
public class BindingRootModel<R, T> implements IModel<T>
{
    private final BindingRoot<R, T> binding;
    private final IModel<R> root;

    public BindingRootModel(IModel<R> root, BindingRoot<R, T> binding)
    {
        this.root = root;
        this.binding = binding;
    }

    public T getObject()
    {
        return binding.getWithRoot(root.getObject());
    }

    public void setObject(T object)
    {
        binding.setWithRoot(root.getObject(), object);
    }

    public void detach()
    {
        root.detach();
    }


}
