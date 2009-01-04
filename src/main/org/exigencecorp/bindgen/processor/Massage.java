package org.exigencecorp.bindgen.processor;

public class Massage {

    public static String packageName(String originalBindingName) {
        if (originalBindingName.startsWith("java")) {
            return "bindgen." + originalBindingName;
        }
        return originalBindingName;
    }

}
