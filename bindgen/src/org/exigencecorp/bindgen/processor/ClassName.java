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

public class ClassName {

    private static final Pattern outerClassName = Pattern.compile("\\.([A-Z]\\w+)\\.");
    private final TypeMirror type;
    private String fullClassNameWithGenerics;
    public boolean isFixingRawType = false;

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

    public String getBindingPathClassDeclaration() {
        ClassName bindingTypeName = new ClassName(this.getBindingType());
        DeclaredType dt = (DeclaredType) this.type;
        if (dt.getTypeArguments().size() == 0) {
            return bindingTypeName.getWithoutGenericPart() + "Path<R>";
        } else {
            return bindingTypeName.getWithoutGenericPart() + "Path" + "<R, " + new TypeVars(dt).genericsWithBounds + ">";
        }
    }

    public String getBindingRootClassDeclaration() {
        ClassName bindingTypeName = new ClassName(this.getBindingType());
        DeclaredType dt = (DeclaredType) this.type;
        if (dt.getTypeArguments().size() == 0) {
            return bindingTypeName.getWithoutGenericPart();
        } else {
            return bindingTypeName.getWithoutGenericPart() + "<" + new TypeVars(dt).genericsWithBounds + ">";
        }
    }

    public String getBindingRootClassSuperClass() {
        ClassName bindingTypeName = new ClassName(this.getBindingType());
        String name;
        DeclaredType dt = (DeclaredType) this.type;
        if (dt.getTypeArguments().size() == 0) {
            name = bindingTypeName.getWithoutGenericPart() + "Path<R>";
        } else {
            name = bindingTypeName.getWithoutGenericPart() + "Path<R, " + new TypeVars(dt).generics + ">";
        }
        return name.replaceFirst("<R", "<" + this.get());
    }

    /** @return binding type, e.g. bindgen.java.lang.StringBinding, bindgen.app.EmployeeBinding */
    public String getBindingType() {
        String bindingName = this.getWithoutGenericPart() + "Binding";
        if (this.hasGenerics() && !this.hasWildcards()) {
            bindingName += this.getGenericPart();
        }
        return "bindgen." + this.lowerCaseOuterClassNames(bindingName);
    }

    public String getBindingTypeForPathWithR() {
        String bindingName = this.getWithoutGenericPart() + "BindingPath";

        if (this.isRawType()) {
            List<String> foo = new ArrayList<String>();
            foo.add("R");
            TypeElement e = (TypeElement) CurrentEnv.get().getTypeUtils().asElement(this.type);
            for (int i = 0; i < e.getTypeParameters().size(); i++) {
                foo.add("?");
            }
            bindingName += "<" + Join.commaSpace(foo) + ">";
        } else if (this.hasGenerics()) {
            bindingName += this.getGenericPart().replaceFirst("<", "<R, ");
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
        String superName = this.lowerCaseOuterClassNames("bindgen." + this.getWithoutGenericPart() + "BindingPath");

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
                dummyParams.add(this.getGenericPartWithoutBrackets());
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
            return this.getWithoutGenericPart() + "<" + Join.commaSpace(dummyParams) + ">";
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
        return this.getGenericPart().length() > 0;
    }

    public boolean hasWildcards() {
        return this.getGenericPart().indexOf('?') > -1;
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
        return this.fullClassNameWithGenerics;
    }

    /** @return "com.app.Type<String, String>" if the type is "com.app.Type<String, String>" */
    public String toString() {
        return this.fullClassNameWithGenerics;
    }

    public void appendGenericType(String type) {
        this.fullClassNameWithGenerics += "<" + type + ">";
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
