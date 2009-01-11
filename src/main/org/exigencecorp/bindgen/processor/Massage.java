package org.exigencecorp.bindgen.processor;

public class Massage {

    public static String packageName(String originalBindingName) {
        if (originalBindingName.startsWith("java")) {
            return "bindgen." + originalBindingName;
        }
        return originalBindingName;
    }

    public static String stripGenerics(String originalName) {
        int firstBracket = originalName.indexOf("<");
        if (firstBracket != -1) {
            return originalName.substring(0, firstBracket);
        }
        return originalName;
    }

}
