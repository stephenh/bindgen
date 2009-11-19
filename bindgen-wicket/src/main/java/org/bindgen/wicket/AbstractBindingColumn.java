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

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.bindgen.BindingRoot;


/**
 * A convenience implementation of column that adds a label to the cell whose model is determined by
 * the provided bindgen binding that is evaluated against the current row's model object
 * <p>
 * Example
 * 
 * <pre>
 * columns[0] = new PropertyColumn(new Model&lt;String&gt;(&quot;First Name&quot;), new PersonBinding().firstName());
 * </pre>
 * 
 * @see BindingRootModel
 * 
 * @author igor.vaynberg
 * @param <T>
 *            The type of the binding
 * 
 */
public abstract class AbstractBindingColumn<R, T> extends AbstractColumn<R>
{
    private static final long serialVersionUID = 1L;

    private final BindingRoot<R, ? extends T> binding;

    /**
     * Creates a property column that is also sortable
     * 
     * @param displayModel
     *            display model
     * @param sortProperty
     *            sort property
     * @param binding
     *            binding
     */
    public AbstractBindingColumn(IModel<String> displayModel, String sortProperty,
            BindingRoot<R, ? extends T> binding)
    {
        super(displayModel, sortProperty);
        this.binding = binding;
    }

    /**
     * Creates a non sortable property column
     * 
     * @param displayModel
     *            display model
     * @param binding
     *            binding
     */
    public AbstractBindingColumn(IModel<String> displayModel, BindingRoot<R, ? extends T> binding)
    {
        super(displayModel, null);
        this.binding = binding;
    }


    /**
     * Creates a property column that is also sortable
     * 
     * @param displayModel
     *            display model
     * @param sortProperty
     *            sort property
     * @param binding
     *            binding
     */
    public AbstractBindingColumn(String headerKey, String sortProperty, BindingRoot<R,? extends T> binding)
    {
        super(new ResourceModel(headerKey), sortProperty);
        this.binding = binding;
    }

    /**
     * Creates a non sortable property column
     * 
     * @param displayModel
     *            display model
     * @param binding
     *            binding
     */
    public AbstractBindingColumn(String headerKey, BindingRoot<R, ? extends T> binding)
    {
        super(new ResourceModel(headerKey), null);
        this.binding = binding;
    }


    /**
     * {@inheritDoc}
     * 
     * @see ICellPopulator#populateItem(Item, String, IModel)
     */
    public void populateItem(Item<ICellPopulator<R>> item, String componentId, IModel<R> rowModel)
    {
        populateItem(item, componentId, new BindingRootModel<R, T>(rowModel, (BindingRoot<R, T>)binding));
    }

    /**
     * Method used to populate a cell in the table
     * 
     * <b>Implementation MUST add a component to the {@code item} using the {@code componentId}
     * provided, otherwise a WicketRuntimeException
     * 
     * @see ICellPopulator#populateItem(Item, String, IModel)
     * @param item
     * @param componentId
     * @param model
     */
    protected abstract void populateItem(Item<ICellPopulator<R>> item, String componentId,
            BindingRootModel<R, T> model);


}
