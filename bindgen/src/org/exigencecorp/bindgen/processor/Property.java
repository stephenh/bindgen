package org.exigencecorp.bindgen.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

import joist.util.Inflector;

import org.exigencecorp.bindgen.AbstractBinding;

/** Given a TypeMirror type of a field/method property, provides information about its binding outer/inner class. */
public class Property {

    private static final Pattern outerClassName = Pattern.compile("\\.([A-Z]\\w+)\\.");
    protected final TypeMirror type;
    protected ClassName2 name;

    public Property(TypeMirror type) {
        this.type = this.boxIfNeeded(type);
        this.name = new ClassName2(this.type.toString());
    }

    /** @return binding type, e.g. bindgen.java.lang.StringBinding, bindgen.app.EmployeeBinding */
    public ClassName2 getBindingType() {
        String bindingName = this.name.getWithoutGenericPart() + "Binding";
        if (this.hasGenerics() && !this.hasWildcards()) {
            bindingName += this.name.getGenericPart();
        }
        return new ClassName2("bindgen." + this.lowerCaseOuterClassNames(bindingName));
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

    // Make this go away
    public String getGenericPartWithoutBrackets() {
        return this.name.getGenericPartWithoutBrackets();
    }

    private TypeMirror boxIfNeeded(TypeMirror type) {
        if (type instanceof PrimitiveType) {
            // double check--Eclipse worked fine but javac is letting non-primitive types in here
            if (type.toString().indexOf('.') == -1) {
                try {
                    return CurrentEnv.get().getTypeUtils().boxedClass((PrimitiveType) type).asType();
                } catch (NullPointerException npe) {
                    return type; // it is probably a type parameter, e.g. T
                }
            }
        }
        return type;
    }

    // Watch for package.Foo.Inner -> package.foo.Inner
    protected String lowerCaseOuterClassNames(String className) {
        Matcher m = outerClassName.matcher(className);
        while (m.find()) {
            className = m.replaceFirst("." + Inflector.uncapitalize(m.group(1)) + ".");
            m = outerClassName.matcher(className);
        }
        return className;
    }
}
