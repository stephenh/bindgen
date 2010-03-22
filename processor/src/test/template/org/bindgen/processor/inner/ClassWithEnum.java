package org.bindgen.processor.inner;

import org.bindgen.Bindable;

@Bindable
public class ClassWithEnum {

    public enum Enum1 { ONE, TWO };
    
    public Enum1 enum1;

}
