package org.exigencecorp.bindgen.processor;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

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
        if (this.hasGenerics() && !this.hasWildcards()) {
            bindingName += this.name.getGenericPart();
        }
        return new ClassName("bindgen." + Util.lowerCaseOuterClassNames(bindingName));
    }

    public String getBindingPathClassDeclaration() {
        DeclaredType dt = (DeclaredType) this.type;
        if (dt.getTypeArguments().size() == 0) {
            return this.getBindingType().getWithoutGenericPart() + "Path<R>";
        } else {
            return this.getBindingType().getWithoutGenericPart() + "Path" + "<R, " + new TypeVars(dt).genericsWithBounds + ">";
        }
    }

    public String getBindingPathClassSuperClass() {
        return AbstractBinding.class.getName() + "<R, " + this.name.get() + ">";
    }

    public String getBindingRootClassDeclaration() {
        DeclaredType dt = (DeclaredType) this.type;
        if (dt.getTypeArguments().size() == 0) {
            return this.getBindingType().getWithoutGenericPart();
        } else {
            return this.getBindingType().getWithoutGenericPart() + "<" + new TypeVars(dt).genericsWithBounds + ">";
        }
    }

    public String getBindingRootClassSuperClass() {
        String name;
        DeclaredType dt = (DeclaredType) this.type;
        if (dt.getTypeArguments().size() == 0) {
            name = this.getBindingType().getWithoutGenericPart() + "Path<R>";
        } else {
            name = this.getBindingType().getWithoutGenericPart() + "Path<R, " + new TypeVars(dt).generics + ">";
        }
        return name.replaceFirst("<R", "<" + this.get());
    }

    public boolean hasGenerics() {
        return this.name.getGenericPart().length() > 0;
    }

    public boolean hasWildcards() {
        return this.name.getGenericPart().indexOf('?') > -1;
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
