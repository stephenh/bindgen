package org.exigencecorp.bindgen.processor;

import java.util.List;

import javax.lang.model.type.TypeMirror;

import joist.util.Join;

import org.exigencecorp.bindgen.AbstractBinding;

/** Given a TypeMirror type of a field/method property, provides information about its binding outer/inner class. */
public class Property {

    private final ClassName name;

    public Property(TypeMirror type) {
        this.name = new ClassName(Util.boxIfNeeded(type).toString());
    }

    /** @return binding type, e.g. bindgen.java.lang.StringBinding, bindgen.app.EmployeeBinding */
    public ClassName getBindingType() {
        String bindingName = "bindgen." + this.name.getWithoutGenericPart() + "Binding" + this.name.getGenericPart();
        return new ClassName(Util.lowerCaseOuterClassNames(bindingName));
    }

    public String getBindingPathClassDeclaration() {
        List<String> typeArgs = this.name.getGenericsWithBounds();
        typeArgs.add(0, "R");
        return this.getBindingType().getWithoutGenericPart() + "Path" + "<" + Join.commaSpace(typeArgs) + ">";
    }

    public String getBindingPathClassSuperClass() {
        return AbstractBinding.class.getName() + "<R, " + this.name.get() + ">";
    }

    public String getBindingRootClassDeclaration() {
        List<String> typeArgs = this.name.getGenericsWithBounds();
        if (typeArgs.size() == 0) {
            return this.getBindingType().getWithoutGenericPart();
        } else {
            return this.getBindingType().getWithoutGenericPart() + "<" + Join.commaSpace(typeArgs) + ">";
        }
    }

    public String getBindingRootClassSuperClass() {
        List<String> typeArgs = this.name.getGenericsWithoutBounds();
        typeArgs.add(0, this.get());
        return this.getBindingType().getWithoutGenericPart() + "Path" + "<" + Join.commaSpace(typeArgs) + ">";
    }

    /** @return "com.app.Type<String, String>" if the type is "com.app.Type<String, String>" */
    public String get() {
        return this.name.get();
    }

    /** @return "com.app.Type<String, String>" if the type is "com.app.Type<String, String>" */
    public String toString() {
        return this.name.get();
    }

}
