package org.exigencecorp.bindgen.example;

import org.exigencecorp.bindgen.Bindable;

import bindgen.org.exigencecorp.bindgen.example.innerClassExample1.InnerClassBinding;

public class InnerClassExample1 {

    public InnerClass newInnerClass() {
        return new InnerClass();
    }

    @Bindable
    public final class InnerClass {
        public String name;
        private InnerClassBinding bind = new InnerClassBinding(this);

        public InnerClass() {
        }

        public InnerClassBinding getBind() {
            return this.bind;
        }
    }

}
