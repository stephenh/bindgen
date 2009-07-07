package org.exigencecorp.bindgen.processor;

import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import joist.util.Join;

import org.exigencecorp.bindgen.AbstractBinding;

/** Given a TypeMirror type of a field/method property, provides information about its binding outer/inner class. */
public class Property {

    protected final TypeMirror type;
    protected final ClassName name;

    public Property(TypeMirror type) {
        this.type = Util.boxIfNeeded(type);
        this.name = new ClassName(this.type.toString());
    }

    /** @return binding type, e.g. bindgen.java.lang.StringBinding, bindgen.app.EmployeeBinding */
    public ClassName getBindingType() {
        String bindingName = this.name.getWithoutGenericPart() + "Binding";
        if (this.name.hasGenerics() && !this.name.hasWildcards()) {
            bindingName += this.name.getGenericPart();
        }
        return new ClassName("bindgen." + Util.lowerCaseOuterClassNames(bindingName));
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
        DeclaredType dt = (DeclaredType) this.type;
        if (dt.getTypeArguments().size() == 0) {
            return this.getBindingType().getWithoutGenericPart();
        } else {
            return this.getBindingType().getWithoutGenericPart() + "<" + Join.commaSpace(this.name.getGenericsWithBounds()) + ">";
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
