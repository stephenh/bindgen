package org.exigencecorp.bindgen.processor.util;

import static org.exigencecorp.bindgen.processor.CurrentEnv.getElementUtils;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeVariable;

public class ClassName {

    private final String fullClassNameWithGenerics;

    public ClassName(String fullClassNameWithGenerics) {
        this.fullClassNameWithGenerics = fullClassNameWithGenerics;
    }

    public String get() {
        return this.fullClassNameWithGenerics;
    }

    public String toString() {
        return this.get();
    }

    /** @return "Type" if the type is "com.app.Type<String, String>" */
    public String getSimpleName() {
        String p = this.getWithoutGenericPart();
        int lastDot = p.lastIndexOf('.');
        if (lastDot == -1) {
            return p;
        } else {
            return p.substring(lastDot + 1);
        }
    }

    /** @return "com.app" if the type is "com.app.Type<String, String>" */
    public String getPackageName() {
        String p = this.getWithoutGenericPart();
        int lastDot = p.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        } else {
            return p.substring(0, lastDot);
        }
    }

    public List<String> getGenericsWithoutBounds() {
        List<String> args = new ArrayList<String>();
        for (TypeVariable tv : (List<TypeVariable>) this.getDeclaredType().getTypeArguments()) {
            args.add(tv.toString());
        }
        return args;
    }

    public List<String> getGenericsWithBounds() {
        List<String> args = new ArrayList<String>();
        for (TypeVariable tv : (List<TypeVariable>) this.getDeclaredType().getTypeArguments()) {
            String arg = tv.toString();
            if (!Util.isOfTypeObjectOrNone(tv.getUpperBound())) {
                arg += " extends " + tv.getUpperBound().toString();
            }
            args.add(arg);
        }
        return args;
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

    public boolean hasGenerics() {
        return this.getGenericPart().length() > 0;
    }

    public boolean hasWildcards() {
        return this.getGenericPart().indexOf('?') > -1;
    }

    private DeclaredType getDeclaredType() {
        return (DeclaredType) getElementUtils().getTypeElement(this.getWithoutGenericPart()).asType();
    }

}
