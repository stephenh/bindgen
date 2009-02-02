package org.exigencecorp.bindgen.processor;

public class Massage {

    public static String packageName(String bindingName) {
        // Watch for package.Foo.Inner -> package.Inner
        return "bindgen." + bindingName.replaceAll("\\.([A-Z]\\w+)\\.", ".");
    }

    public static String stripGenerics(String originalName) {
        int firstBracket = originalName.indexOf("<");
        if (firstBracket != -1) {
            return originalName.substring(0, firstBracket);
        }
        return originalName;
    }

}
