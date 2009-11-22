/*
 * $Id: EditContactPage.java 634 2006-03-26 18:28:10 -0800 (Sun, 26 Mar 2006) ivaynberg $
 * $Revision: 634 $
 * $Date: 2006-03-26 18:28:10 -0800 (Sun, 26 Mar 2006) $
 *
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.bindgen.wicket.phonebook.web.page;


import static org.bindgen.BindKeyword.*;
import static org.bindgen.wicket.Bindings.*;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.bindgen.Bindable;
import org.bindgen.wicket.phonebook.Contact;
import org.bindgen.wicket.phonebook.ContactDao;


/**
 * Edit the Contact. Display details if an existing contact, then persist them if saved.
 * 
 * @author igor
 * 
 */
@Bindable
public class EditContactPage extends BasePage
{
    private final Page backPage;
    @SpringBean(name = "contactDao")
    private ContactDao contactDao;

    final Contact contact;

    /**
     * Constructor. Create or edit the contact. Note that if you don't need the page to be
     * bookmarkable, you can use whatever constructor you need, such as is done here.
     * 
     * @param backPage
     *            The page that the user was on before coming here
     * @param contactModel
     *            Model that contains the contact we will edit
     */
    public EditContactPage(Page backPage, IModel< ? > contactModel)
    {
        this.backPage = backPage;

        contact = (Contact)contactModel.getObject();

        Form< ? > form = new Form<Void>("contactForm");
        add(form);

        form.add(new TextField<String>("firstname", model(bind(this).contact().firstname()))
                .setRequired(true).add(StringValidator.maximumLength(32)));

        form.add(new TextField<String>("lastname", model(bind(this).contact().lastname()))
                .setRequired(true).add(StringValidator.maximumLength(32)));

        form.add(new TextField<String>("phone", model(bind(this).contact().phone())).setRequired(
                true).add(StringValidator.maximumLength(16)));

        form.add(new TextField<String>("email", model(bind(this).contact().email()))
                .setRequired(true).add(StringValidator.maximumLength(128)).add(
                        EmailAddressValidator.getInstance()));

        form.add(new Button("cancel", new ResourceModel("cancel"))
        {
            @Override
            public void onSubmit()
            {
                onCancel();
            }
        }.setDefaultFormProcessing(false));

        form.add(new Button("save", new ResourceModel("save"))
        {
            @Override
            public void onSubmit()
            {
                onSave();
            }
        });
    }


    private void onCancel()
    {
        String msg = getLocalizer().getString("status.cancel", this);
        getSession().info(msg);
        setResponsePage(EditContactPage.this.backPage);

    }

    private void onSave()
    {
        contactDao.save(contact);
        String msg = MapVariableInterpolator.interpolate(getLocalizer().getString("status.save",
                this), new MicroMap<String, String>("name", contact.getFullName()));
        getSession().info(msg);
        setResponsePage(EditContactPage.this.backPage);
    }

}
