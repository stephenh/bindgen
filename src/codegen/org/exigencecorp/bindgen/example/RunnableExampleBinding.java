package org.exigencecorp.bindgen.example;

import org.exigencecorp.bindgen.Binding;

public class RunnableExampleBinding implements Binding<RunnableExample> {

    private RunnableExample value;
    private Runnable doStuff;

    public RunnableExampleBinding() {
    }

    public RunnableExampleBinding(RunnableExample value) {
        this.set(value);
    }

    public void set(RunnableExample value) {
        this.value = value;
    }

    public RunnableExample get() {
        return this.value;
    }

    public String getName() {
        return "";
    }

    public Class<RunnableExample> getType() {
        return RunnableExample.class;
    }

    public Runnable doStuff() {
        if (this.doStuff == null) {
            this.doStuff = new MyDoStuffBinding();
        }
        return this.doStuff;
    }

    public class MyDoStuffBinding implements Runnable {
        public void run() {
            RunnableExampleBinding.this.get().doStuff();
        }
    }

}
