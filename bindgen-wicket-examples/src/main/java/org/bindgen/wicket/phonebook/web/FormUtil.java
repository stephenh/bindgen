package org.bindgen.wicket.phonebook.web;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.bindgen.BindingRoot;
import org.bindgen.wicket.BindingModel;


public class FormUtil
{
    private FormUtil()
    {

    }

    public static <R, T> IModel<T> model(BindingRoot<R, T> binding)
    {
        return new BindingModel<T>(binding);
    }

    public static <T> FormComponent<T> required(FormComponent<T> fc)
    {
        return fc.setRequired(true);
    }


    public static TextField<String> email(TextField<String> tf)
    {
        tf.add(EmailAddressValidator.getInstance());
        return tf;
    }

    public static TextField<String> maxlen(TextField<String> tf, int max)
    {
        tf.add(StringValidator.maximumLength(max));
        return tf;
    }

    public static Button cancel(Button b)
    {
        return b.setDefaultFormProcessing(false);
    }

    public static Button labelled(Button b, String key)
    {
        b.setModel(new ResourceModel(key));
        return b;
    }

}
