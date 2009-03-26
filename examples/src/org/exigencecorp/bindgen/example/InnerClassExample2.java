package org.exigencecorp.bindgen.example;

import org.exigencecorp.bindgen.Bindable;

import bindgen.org.exigencecorp.bindgen.example.innerClassExample2.InnerClassBinding;

public class InnerClassExample2 {

    public InnerClass newInnerClass() {
        return new InnerClass();
    }

    @Bindable
    public class InnerClass {
        public String different;

        public InnerClass() {
        }

        public InnerClassBinding getBind() {
            return new InnerClassBinding(this);
        }
    }

}
