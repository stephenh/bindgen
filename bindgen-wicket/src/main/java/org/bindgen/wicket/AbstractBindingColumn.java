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

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;
import org.bindgen.Binding;
import org.bindgen.BindingRoot;

public abstract class AbstractBindingColumn<R, T> implements IStyledColumn<R>
{
    private static final long serialVersionUID = 1L;

    private final BindingRoot<R, ? extends T> binding;
    private String sortProperty;
    private IModel<String> header;

    public AbstractBindingColumn(BindingRoot<R, ? extends T> binding)
    {
        this.binding = binding;
    }

    /**
     * {@inheritDoc}
     * 
     * @see ICellPopulator#populateItem(Item, String, IModel)
     */
    public void populateItem(Item<ICellPopulator<R>> item, String componentId, IModel<R> rowModel)
    {
        populateItem(item, componentId, new BindingRootModel<R, T>(rowModel,
                (BindingRoot<R, T>)binding));
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

    public String getCssClass()
    {
        return null;
    }

    public void detach()
    {

    }

    public Component getHeader(String componentId)
    {
        if (header != null)
        {
            return new Label(componentId, header);
        }
        else
        {
            return new Label(componentId, "");
        }
    }

    public String getSortProperty()
    {
        return sortProperty;
    }

    public boolean isSortable()
    {
        return !Strings.isEmpty(sortProperty);
    }

    public AbstractBindingColumn<R, T> setSort(String sortProperty)
    {
        this.sortProperty = sortProperty;
        return this;
    }

    public AbstractBindingColumn<R, T> setSort(Binding< ? > binding)
    {
        return setSort(binding.getPath());
    }


    public AbstractBindingColumn<R, T> setHeader(IModel<String> header)
    {
        this.header = header;
        return this;
    }

    public AbstractBindingColumn<R, T> setHeader(String headerKey)
    {
        return setHeader(new ResourceModel(headerKey));
    }

    public AbstractBindingColumn<R, T> setSortToData()
    {
        setSort(binding);
        return this;
    }

}
