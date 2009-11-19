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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.bindgen.BindingRoot;


/**
 * A convenience implementation of column that adds a label to the cell whose model is determined by
 * the provided bindgen binding that is evaluated against the current row's model object
 * <p>
 * Example
 * 
 * <pre>
 * columns[0] = new StringBindingColumn(new Model&lt;String&gt;(&quot;First Name&quot;), new PersonBinding()
 *         .firstName());
 * </pre>
 * 
 * @see BindingRootModel
 * 
 * @author igor.vaynberg
 * @param <T>
 *            The type of the binding
 * 
 */
public class BindingColumn<R> extends AbstractBindingColumn<R, Object>
{


    public BindingColumn(IModel<String> displayModel, BindingRoot<R, ? extends Object> binding)
    {
        super(displayModel, binding);
    }

    public BindingColumn(IModel<String> displayModel, String sortProperty,
            BindingRoot<R, ? extends Object> binding)
    {
        super(displayModel, sortProperty, binding);
    }

    public BindingColumn(String headerKey, BindingRoot<R, ? extends Object> binding)
    {
        super(headerKey, binding);
    }

    public BindingColumn(String headerKey, String sortProperty, BindingRoot<R, ? extends Object> binding)
    {
        super(headerKey, sortProperty, binding);
    }

    @Override
    protected void populateItem(Item<ICellPopulator<R>> item, String componentId,
            BindingRootModel<R, Object> model)
    {
        item.add(new Label(componentId, model));
    }


}
