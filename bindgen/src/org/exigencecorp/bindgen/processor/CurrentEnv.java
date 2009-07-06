package org.exigencecorp.bindgen.processor;

import javax.annotation.processing.ProcessingEnvironment;

public class CurrentEnv {

    private static final ThreadLocal<ProcessingEnvironment> current = new ThreadLocal<ProcessingEnvironment>();

    public static void set(ProcessingEnvironment env) {
        current.set(env);
    }

    public static void unset() {
        current.set(null);
    }

    public static ProcessingEnvironment get() {
        return current.get();
    }

}
