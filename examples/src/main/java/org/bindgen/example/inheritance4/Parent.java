package org.bindgen.examples.inheritance4;

import org.bindgen.Bindable;

public interface Parent<T> {
    T getParentField();
    
    void setParentField(T field);
}
