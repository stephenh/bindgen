package org.exigencecorp.bindgen.example.inheritance2;

import org.exigencecorp.bindgen.Bindable;

@Bindable
public class SubExample extends MidExample<SubExample> {

    public String name;
    public String subOnly;

}
