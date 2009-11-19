package org.bindgen.wicket;

import org.apache.wicket.model.IModel;
import org.bindgen.BindingRoot;

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
