package org.bindgen.example.wildcards;

import org.bindgen.Bindable;

@Bindable
public class Test2<T extends Test2<T>> {

}
