package org.exigencecorp.bindgen.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.type.TypeMirror;

import org.apache.commons.lang.StringUtils;

public class ClassName {

    private static final Pattern p = Pattern.compile("\\.([A-Z]\\w+)\\.");
    private String fullClassNameWithGenerics;

    public ClassName(TypeMirror type) {
        this.fullClassNameWithGenerics = type.toString();
    }

    /** @return "<String, String>" if the type is "com.app.Type<String, String>" or "" if no generics */
    public String getGenericPart() {
        int firstBracket = this.fullClassNameWithGenerics.indexOf("<");
        if (firstBracket != -1) {
            return this.fullClassNameWithGenerics.substring(firstBracket);
        }
        return "";
    }

    /** @return "com.app.Type" if the type is "com.app.Type<String, String>" */
    public String getWithoutGenericPart() {
        int firstBracket = this.fullClassNameWithGenerics.indexOf("<");
        if (firstBracket != -1) {
            return this.fullClassNameWithGenerics.substring(0, firstBracket);
        }
        return this.fullClassNameWithGenerics;
    }

    /** @return binding type, e.g. bindgen.java.lang.StringBinding, bindgen.app.EmployeeBinding */
    public String getBindingType() {
        // Watch for package.Foo.Inner -> package.foo.Inner
        String bindingName = this.getWithoutGenericPart() + "Binding" + this.getGenericPart();
        Matcher m = p.matcher(bindingName);
        while (m.find()) {
            bindingName = m.replaceFirst("." + StringUtils.uncapitalize(m.group(1)) + ".");
            m = p.matcher(bindingName);
        }
        return "bindgen." + bindingName;
    }

    /** @return "com.app.Type<String, String>" if the type is "com.app.Type<String, String>" */
    public String get() {
        return this.fullClassNameWithGenerics;
    }

    public String toString() {
        return this.fullClassNameWithGenerics;
    }

    public void appendGenericType(String type) {
        this.fullClassNameWithGenerics += "<" + type + ">";
    }
}
