package org.exigencecorp.bindgen.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class Massage {

    private static final Pattern p = Pattern.compile("\\.([A-Z]\\w+)\\.");

    public static String packageName(String bindingName) {
        // Watch for package.Foo.Inner -> package.foo.Inner
        Matcher m = p.matcher(bindingName);
        while (m.find()) {
            bindingName = m.replaceFirst("." + StringUtils.uncapitalize(m.group(1)) + ".");
            m = p.matcher(bindingName);
        }
        return "bindgen." + bindingName;
    }

    public static String stripGenerics(String originalName) {
        int firstBracket = originalName.indexOf("<");
        if (firstBracket != -1) {
            return originalName.substring(0, firstBracket);
        }
        return originalName;
    }

}
