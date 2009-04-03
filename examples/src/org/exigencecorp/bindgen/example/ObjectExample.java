package org.exigencecorp.bindgen.example;

import org.exigencecorp.bindgen.Bindable;

@Bindable
public class ObjectExample {

    public Object value;
    public Class<?> clazz;

    public Class<?> getOtherClazz() {
        return null;
    }
}
