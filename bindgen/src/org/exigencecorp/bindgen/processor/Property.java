package org.exigencecorp.bindgen.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import joist.util.Inflector;
import joist.util.Join;

/** Given a TypeMirror type of a field/method property, provides information about its binding outer/inner class. */
public class Property {

    private static final Pattern outerClassName = Pattern.compile("\\.([A-Z]\\w+)\\.");
    private final TypeMirror type;
    private final ClassName2 name;
    public boolean isFixingRawType = false;

    public Property(TypeMirror type) {
        this.type = type;
        this.name = new ClassName2(type.toString());
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

    public String getBindingRootClassInstantiation(String propertyName) {
        String name = "My" + Inflector.capitalize(propertyName) + "Binding";
        if (this.type instanceof DeclaredType) {
            List<String> dummyParams = new ArrayList<String>();
            DeclaredType dt = (DeclaredType) this.type;
            for (TypeMirror tm : dt.getTypeArguments()) {
                if (tm instanceof WildcardType) {
                    dummyParams.add("Object");
                }
            }
            if (dummyParams.size() > 0) {
                name += "<" + Join.commaSpace(dummyParams) + ">";
            }
        }
        return name;
    }

    public String getBindingClassFieldDeclaration(String propertyName) {
        String name = "My" + Inflector.capitalize(propertyName) + "Binding";
        if (this.type instanceof DeclaredType) {
            List<String> dummyParams = new ArrayList<String>();
            DeclaredType dt = (DeclaredType) this.type;
            for (TypeMirror tm : dt.getTypeArguments()) {
                if (tm instanceof WildcardType) {
                    dummyParams.add("?");
                }
            }
            if (dummyParams.size() > 0) {
                name += "<" + Join.commaSpace(dummyParams) + ">";
            }
        }
        return name;
    }

    public String getBindingTypeForPathWithR() {
        String bindingName = this.name.getWithoutGenericPart() + "BindingPath";

        if (this.isRawType()) {
            List<String> foo = new ArrayList<String>();
            foo.add("R");
            TypeElement e = (TypeElement) CurrentEnv.get().getTypeUtils().asElement(this.type);
            for (int i = 0; i < e.getTypeParameters().size(); i++) {
                foo.add("?");
            }
            bindingName += "<" + Join.commaSpace(foo) + ">";
        } else if (this.hasGenerics()) {
            bindingName += this.name.getGenericPart().replaceFirst("<", "<R, ");
        } else {
            bindingName += "<R>";
        }

        bindingName = bindingName.replaceAll(" super \\w+", ""); // for Class.getSuperClass()

        return "bindgen." + this.lowerCaseOuterClassNames(bindingName);
    }

    public String getInnerClass(String propertyName) {
        String name = "My" + Inflector.capitalize(propertyName) + "Binding";

        if (this.type.getKind() == TypeKind.DECLARED) {
            List<String> dummyParams = new ArrayList<String>();
            DeclaredType dt = (DeclaredType) this.type;
            if (!this.isRawType()) {
                for (TypeMirror tm : dt.getTypeArguments()) {
                    if (tm instanceof WildcardType) {
                        dummyParams.add("U" + dummyParams.size());
                    }
                }
            } else {
                TypeElement e = (TypeElement) CurrentEnv.get().getTypeUtils().asElement(dt);
                for (TypeParameterElement tpe : e.getTypeParameters()) {
                    dummyParams.add(tpe.toString());
                }
            }
            if (dummyParams.size() > 0) {
                name += "<" + Join.commaSpace(dummyParams) + ">";
            }
        }

        return name;
    }

    public String getInnerClassSuperClass() {
        String superName = this.lowerCaseOuterClassNames("bindgen." + this.name.getWithoutGenericPart() + "BindingPath");

        DeclaredType dt = (DeclaredType) this.type;
        TypeElement te = (TypeElement) CurrentEnv.get().getTypeUtils().asElement(dt);

        if (this.isRawType() || this.hasGenerics()) {
            List<String> dummyParams = new ArrayList<String>();
            dummyParams.add("R");

            if (this.isRawType()) {
                for (TypeParameterElement tpe : te.getTypeParameters()) {
                    dummyParams.add(tpe.toString());
                }
            } else if (!this.isFixingRawType) {
                for (TypeMirror tm : dt.getTypeArguments()) {
                    if (tm instanceof WildcardType) {
                        dummyParams.add("U" + (dummyParams.size() - 1));
                    } else {
                        dummyParams.add(tm.toString());
                    }
                }
            } else {
                dummyParams.add(this.name.getGenericPartWithoutBrackets());
            }
            superName += "<" + Join.commaSpace(dummyParams) + ">";
        } else {
            superName += "<R>";
        }

        return superName;
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
            return this.name.getWithoutGenericPart() + "<" + Join.commaSpace(dummyParams) + ">";
        }
        return this.get();
    }

    /** Add generic suffixes to avoid warnings in bindings for pre-1.5 APIs.
     *
     * This is for old pre-1.5 APIs that use, say, Enumeration. We upgrade it
     * to something like Enumeration<String> based on the user configuration,
     * e.g.:
     *
     * <code>fixRawType.javax.servlet.http.HttpServletRequest.attributeNames=String</code>
     *
     */
    public void fixRawTypeIfNeeded(TypeElement enclosed, String propertyName) {
        String configKey = "fixRawType." + enclosed.toString() + "." + propertyName;
        String configValue = CurrentEnv.get().getOptions().get(configKey);
        if (!this.hasGenerics() && configValue != null) {
            this.appendGenericType(configValue);
            this.isFixingRawType = true;
        }
    }

    public String getCastForReturnIfNeeded() {
        return this.hasWildcards() ? "(" + this.getSetType() + ") " : "";
    }

    public boolean hasGenerics() {
        return this.name.getGenericPart().length() > 0;
    }

    public boolean hasWildcards() {
        return this.name.getGenericPart().indexOf('?') > -1;
    }

    public boolean isRawType() {
        if (this.isFixingRawType) {
            return false;
        }
        if (this.type.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) this.type;
            TypeElement te = (TypeElement) CurrentEnv.get().getTypeUtils().asElement(dt);
            return dt.getTypeArguments().size() != te.getTypeParameters().size();
        }
        return false;
    }

    /** @return "com.app.Type<String, String>" if the type is "com.app.Type<String, String>" */
    public String get() {
        return this.name.get();
    }

    /** @return "com.app.Type<String, String>" if the type is "com.app.Type<String, String>" */
    public String toString() {
        return this.name.get();
    }

    public void appendGenericType(String type) {
        this.fullClassNameWithGenerics += "<" + type + ">";
    }

    public boolean isForListOrSet() {
        return "java.util.List".equals(this.name.getWithoutGenericPart()) || "java.util.Set".equals(this.name.getWithoutGenericPart());
    }

    public boolean isForBinding() {
        return this.name.getWithoutGenericPart().endsWith("Binding");
    }

    // Make this go away
    public String getGenericPartWithoutBrackets() {
        return this.name.getGenericPartWithoutBrackets();
    }

    // Watch for package.Foo.Inner -> package.foo.Inner
    private String lowerCaseOuterClassNames(String className) {
        Matcher m = outerClassName.matcher(className);
        while (m.find()) {
            className = m.replaceFirst("." + Inflector.uncapitalize(m.group(1)) + ".");
            m = outerClassName.matcher(className);
        }
        return className;
    }
}
