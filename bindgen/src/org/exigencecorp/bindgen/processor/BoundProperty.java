package org.exigencecorp.bindgen.processor;

import static org.exigencecorp.bindgen.processor.CurrentEnv.getOption;
import static org.exigencecorp.bindgen.processor.CurrentEnv.getTypeUtils;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import joist.util.Inflector;
import joist.util.Join;

/** Given a TypeMirror type of a field/method property, provides information about its binding outer/inner class. */
public class BoundProperty {

    private final TypeMirror type;
    private final String propertyName;
    private final TypeElement enclosing;
    private final Element element;
    private final boolean isFixingRawType;
    private ClassName name;

    public BoundProperty(TypeMirror type, Element enclosed, String propertyName) {
        this.type = Util.boxIfNeeded(type);
        this.name = new ClassName(this.type.toString());
        this.enclosing = (TypeElement) enclosed.getEnclosingElement();
        this.element = getTypeUtils().asElement(this.type);
        this.propertyName = propertyName;
        this.isFixingRawType = this.fixRawTypeIfNeeded();
    }

    public boolean isForGenericTypeParameter() {
        return this.isTypeParameter(this.element);
    }

    public boolean isNameless() {
        return this.propertyName == null || "get".equals(this.propertyName) || "declaringClass".equals(this.propertyName) || this.element == null;
    }

    public boolean shouldSkip() {
        String configKey = "skipAttribute." + this.enclosing.toString() + "." + this.propertyName;
        String configValue = getOption(configKey);
        return "true".equals(configValue);
    }

    public String getCastForReturnIfNeeded() {
        return this.hasWildcards() ? "(" + this.getSetType() + ") " : "";
    }

    public String getBindingRootClassInstantiation() {
        String name = "My" + Inflector.capitalize(this.propertyName) + "Binding";
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

    public String getBindingClassFieldDeclaration() {
        String name = "My" + Inflector.capitalize(this.propertyName) + "Binding";
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
            TypeElement e = (TypeElement) getTypeUtils().asElement(this.type);
            for (int i = 0; i < e.getTypeParameters().size(); i++) {
                foo.add("?");
            }
            bindingName += "<" + Join.commaSpace(foo) + ">";
        } else if (this.name.hasGenerics()) {
            bindingName += this.name.getGenericPart().replaceFirst("<", "<R, ");
        } else {
            bindingName += "<R>";
        }
        bindingName = bindingName.replaceAll(" super \\w+", ""); // for Class.getSuperClass()
        return "bindgen." + Util.lowerCaseOuterClassNames(bindingName);
    }

    public String getInnerClass() {
        String name = "My" + Inflector.capitalize(this.propertyName) + "Binding";
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
                TypeElement e = (TypeElement) getTypeUtils().asElement(dt);
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
        String superName = Util.lowerCaseOuterClassNames("bindgen." + this.name.getWithoutGenericPart() + "BindingPath");
        DeclaredType dt = (DeclaredType) this.type;
        TypeElement te = (TypeElement) getTypeUtils().asElement(dt);
        if (this.isRawType() || this.name.hasGenerics()) {
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

    // Make this go away
    public String getGenericPartWithoutBrackets() {
        return this.name.getGenericPartWithoutBrackets();
    }

    public String getName() {
        return this.propertyName;
    }

    public TypeElement getElement() {
        return this.isTypeParameter(this.element) ? null : (TypeElement) this.element;
    }

    public TypeParameterElement getGenericElement() {
        return this.isTypeParameter(this.element) ? (TypeParameterElement) this.element : null;
    }

    public boolean isForListOrSet() {
        return "java.util.List".equals(this.name.getWithoutGenericPart()) || "java.util.Set".equals(this.name.getWithoutGenericPart());
    }

    public boolean isForBinding() {
        return this.name.getWithoutGenericPart().endsWith("Binding");
    }

    private boolean isTypeParameter(Element element) {
        return element != null && element.getKind() == ElementKind.TYPE_PARAMETER;
    }

    public boolean isRawType() {
        if (this.isFixingRawType) {
            return false;
        }
        if (this.type.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) this.type;
            TypeElement te = (TypeElement) getTypeUtils().asElement(dt);
            return dt.getTypeArguments().size() != te.getTypeParameters().size();
        }
        return false;
    }

    public boolean matchesTypeParameterOfParent() {
        String type = this.getGenericPartWithoutBrackets();
        if (this.hasWildcards()) {
            return true;
        }
        for (TypeParameterElement e : this.enclosing.getTypeParameters()) {
            if (e.toString().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFixingRawType() {
        return this.isFixingRawType;
    }

    /** @return "com.app.Type<String, String>" if the type is "com.app.Type<String, String>" */
    public String get() {
        return this.name.get();
    }

    public boolean hasWildcards() {
        return this.name.hasWildcards();
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
    private boolean fixRawTypeIfNeeded() {
        String configKey = "fixRawType." + this.enclosing.toString() + "." + this.propertyName;
        String configValue = getOption(configKey);
        if (!this.name.hasGenerics() && configValue != null) {
            this.name = new ClassName(this.type.toString() + "<" + configValue + ">");
            return true;
        }
        return false;
    }

}
