package org.exigencecorp.bindgen.example;

import java.util.List;

import org.exigencecorp.bindgen.Bindable;

@Bindable
public class GenericsThreeExample<T> {

    private List<String> strings;
    private List<T> things;

    public List<T> getThings() {
        return this.things;
    }

    public List<String> getStrings() {
        return this.strings;
    }

}
