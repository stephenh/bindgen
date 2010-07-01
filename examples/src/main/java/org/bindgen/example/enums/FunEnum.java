package org.bindgen.examples.enums;

import org.bindgen.Bindable;

@Bindable
public enum FunEnum {
    FIRST(100), SECOND(50);

    private int funLevel;
    
    private FunEnum(int newFunLevel) {
        funLevel = newFunLevel;
    }
    
    public int getFunLevel() {
        return funLevel;
    }
    
    public void setFunLevel(int newFunLevel) {
        funLevel = newFunLevel;
    }
}
