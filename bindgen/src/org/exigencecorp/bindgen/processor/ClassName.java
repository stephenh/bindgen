package org.exigencecorp.bindgen.processor;

import javax.lang.model.type.TypeMirror;

public class ClassName {

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
