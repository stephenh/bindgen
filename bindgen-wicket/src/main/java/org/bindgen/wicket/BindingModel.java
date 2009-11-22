/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bindgen.wicket;

import org.apache.wicket.model.IModel;
import org.bindgen.Binding;

/**
 * A model that uses a {@link Binding} to push and pull data
 * 
 * <p>
 * Example:
 * 
 * <pre>
 * &#064;Bindable
 * public class EditPersonPage extends Webpage {
 *  IModel&lt;Person&gt; person;
 *  
 *  public EditPersonPage(IModel&lt;Person&gt; person) {
 *    this.person=person;
 *    
 *    Form&lt;?&gt; form=new Form&lt;Void&gt;("form");
 *    form.add(new TextField&lt;String&gt;("street1", new BindingModel&lt;String&gt;(new EditPersonPageBinding(this).person().address().street1()));
 * </pre>
 * 
 * or using the shorthand provided by {@link Bindings} class:
 * 
 * <pre>
 * import static org.bindgen.wicket.Bindings.*;
 * import static org.bindgen.BindKeyword.*;
 * 
 * &#064;Bindable
 * public class EditPersonPage extends Webpage {
 *  IModel&lt;Person&gt; person;
 *  
 *  public EditPersonPage(IModel&lt;Person&gt; person) {
 *    this.person=person;
 *    
 *    Form&lt;?&gt; form=new Form&lt;Void&gt;("form");
 *    form.add(new TextField&lt;String&gt;("street1", model(bind(this).person().address().street1()));
 * </pre>
 * 
 * </p>
 * 
 * @author igor.vaynberg
 * 
 * @param <T>
 */
public class BindingModel<T> implements IModel<T>
{
    private final Binding<T> binding;

    /**
     * Constructor
     * 
     * @param binding
     */
    public BindingModel(Binding<T> binding)
    {
        this.binding = binding;
    }

    /** {@inheritDoc} */
    public T getObject()
    {
        return binding.get();
    }

    /** {@inheritDoc} */
    public void setObject(T object)
    {
        binding.set(object);
    }

    /** {@inheritDoc} */
    public void detach()
    {
    }

}
