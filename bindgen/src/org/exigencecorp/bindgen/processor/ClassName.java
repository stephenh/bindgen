package org.exigencecorp.bindgen.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import joist.util.Inflector;
import joist.util.Join;

public class ClassName {

    private static final Pattern outerClassName = Pattern.compile("\\.([A-Z]\\w+)\\.");
    private final TypeMirror type;
    private String fullClassNameWithGenerics;

    public ClassName(TypeMirror type) {
        this.type = type;
        this.fullClassNameWithGenerics = type.toString();
    }

    public ClassName(String fullClassNameWithGenerics) {
        this.type = null;
        this.fullClassNameWithGenerics = fullClassNameWithGenerics;
    }

    /** @return "<String, String>" if the type is "com.app.Type<String, String>" or "" if no generics */
    public String getGenericPart() {
        int firstBracket = this.fullClassNameWithGenerics.indexOf("<");
        if (firstBracket != -1) {
            return this.fullClassNameWithGenerics.substring(firstBracket);
        }
        return "";
    }

    /** @return "String, String" if the type is "com.app.Type<String, String>" or "" if no generics */
    public String getGenericPartWithoutBrackets() {
        String type = this.getGenericPart();
        return type.substring(1, type.length() - 1);
    }

    /** @return "com.app.Type" if the type is "com.app.Type<String, String>" */
    public String getWithoutGenericPart() {
        int firstBracket = this.fullClassNameWithGenerics.indexOf("<");
        if (firstBracket != -1) {
            return this.fullClassNameWithGenerics.substring(0, firstBracket);
        }
        return this.fullClassNameWithGenerics;
    }

    public String getBindingTypeForPathWithR() {
        String bindingName = this.getWithoutGenericPart() + "BindingPath";
        if (this.hasGenerics()) {
            bindingName += this.getGenericPart().replaceFirst("<", "<R, ");
        } else {
            bindingName += "<R>";
        }
        bindingName = bindingName.replaceAll(" super \\w+", ""); // for Class.getSuperClass()
        // Watch for package.Foo.Inner -> package.foo.Inner
        Matcher m = outerClassName.matcher(bindingName);
        while (m.find()) {
            bindingName = m.replaceFirst("." + Inflector.uncapitalize(m.group(1)) + ".");
            m = outerClassName.matcher(bindingName);
        }
        return "bindgen." + bindingName;
    }

    /** @return binding type, e.g. bindgen.java.lang.StringBinding, bindgen.app.EmployeeBinding */
    public String getBindingType() {
        String bindingName = this.getWithoutGenericPart() + "Binding";
        if (this.hasGenerics() && !this.hasWildcards()) {
            bindingName += this.getGenericPart();
        }
        // Watch for package.Foo.Inner -> package.foo.Inner
        Matcher m = outerClassName.matcher(bindingName);
        while (m.find()) {
            bindingName = m.replaceFirst("." + Inflector.uncapitalize(m.group(1)) + ".");
            m = outerClassName.matcher(bindingName);
        }
        return "bindgen." + bindingName;
    }

    /** @return the type appropriate for setter/return arguments. */
    public String getSetType() {
        if (this.hasWildcards()) {
            List<String> dummyParams = new ArrayList<String>();
            if (this.type instanceof DeclaredType) {
                DeclaredType dt = (DeclaredType) this.type;
                for (TypeMirror tm : dt.getTypeArguments()) {
                    if (tm instanceof WildcardType) {
                        dummyParams.add("U" + (dummyParams.size()));
                    } else {
                        dummyParams.add(tm.toString());
                    }
                }
            }
            return this.getWithoutGenericPart() + "<" + Join.commaSpace(dummyParams) + ">";
        }
        return this.get();
    }

    public String getCastForReturnIfNeeded() {
        return this.hasWildcards() ? "(" + this.getSetType() + ") " : "";
    }

    public boolean hasGenerics() {
        return this.getGenericPart().length() > 0;
    }

    public boolean hasWildcards() {
        return this.getGenericPart().indexOf('?') > -1;
    }

    /** @return "com.app.Type<String, String>" if the type is "com.app.Type<String, String>" */
    public String get() {
        return this.fullClassNameWithGenerics;
    }

    /** @return "com.app.Type<String, String>" if the type is "com.app.Type<String, String>" */
    public String toString() {
        return this.fullClassNameWithGenerics;
    }

    public void appendGenericType(String type) {
        this.fullClassNameWithGenerics += "<" + type + ">";
    }
}
