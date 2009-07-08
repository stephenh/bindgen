package org.exigencecorp.bindgen.processor.util;

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

import joist.util.Copy;
import joist.util.Inflector;
import joist.util.Join;

import org.exigencecorp.bindgen.AbstractBinding;

/** Given a TypeMirror type of a field/method property, provides information about its binding outer/inner class. */
public class BoundProperty {

    private final TypeElement enclosing;
    private final TypeMirror type;
    private final Element element;
    private final String propertyName;
    private final boolean isFixingRawType;
    private ClassName name;

    public BoundProperty(Element enclosed, TypeMirror type, String propertyName) {
        this.enclosing = (TypeElement) enclosed.getEnclosingElement();
        this.type = Util.boxIfNeeded(type);
        this.element = getTypeUtils().asElement(this.type);
        this.propertyName = propertyName;
        this.name = new ClassName(this.type.toString());
        this.isFixingRawType = this.fixRawTypeIfNeeded();
    }

    public boolean isForGenericTypeParameter() {
        return this.isTypeParameter(this.element);
    }

    public boolean shouldSkip() {
        return this.element == null || this.isNameless() || this.isSkipAttributeSet() || this.isForBinding();
    }

    public String getCastForReturnIfNeeded() {
        return this.hasWildcards() ? "(" + this.getSetType() + ") " : "";
    }

    private String optionalGenericsIfWildcards(String replace) {
        if (this.type instanceof DeclaredType) {
            List<String> dummyParams = new ArrayList<String>();
            if (!this.isRawType()) {
                for (TypeMirror tm : ((DeclaredType) this.type).getTypeArguments()) {
                    if (tm instanceof WildcardType) {
                        dummyParams.add(replace);
                    }
                }
            }
            if (dummyParams.size() > 0) {
                return "<" + Join.commaSpace(dummyParams) + ">";
            }
        }
        return "";
    }

    public String getBindingRootClassInstantiation() {
        return "My" + Inflector.capitalize(this.propertyName) + "Binding" + this.optionalGenericsIfWildcards("Object");
    }

    public String getBindingClassFieldDeclaration() {
        return "My" + Inflector.capitalize(this.propertyName) + "Binding" + this.optionalGenericsIfWildcards("?");
    }

    public String getInnerClassDeclaration() {
        String name = "My" + Inflector.capitalize(this.propertyName) + "Binding";
        if (this.type.getKind() == TypeKind.DECLARED) {
            List<String> dummyParams = new ArrayList<String>();
            if (this.isRawType()) {
                for (TypeParameterElement tpe : this.getElement().getTypeParameters()) {
                    dummyParams.add(tpe.toString());
                }
            } else {
                for (TypeMirror tm : ((DeclaredType) this.type).getTypeArguments()) {
                    if (tm instanceof WildcardType) {
                        dummyParams.add("U" + dummyParams.size());
                    }
                }
            }
            if (dummyParams.size() > 0) {
                name += "<" + Join.commaSpace(dummyParams) + ">";
            }
        }
        return name;
    }

    public String getInnerClassSuperClass() {
        if (this.isForGenericTypeParameter()) {
            // Being a generic type, we have no XxxBindingPath to extend, so just extend AbstractBinding directly
            return AbstractBinding.class.getName() + "<R, " + this.getGenericElement() + ">";
        }

        String superName = Util.lowerCaseOuterClassNames("bindgen." + this.name.getWithoutGenericPart() + "BindingPath");
        List<String> typeArgs = Copy.list("R");
        if (this.isRawType()) {
            for (TypeParameterElement tpe : this.getElement().getTypeParameters()) {
                typeArgs.add(tpe.toString());
            }
        } else if (this.isFixingRawType()) {
            typeArgs.add(this.name.getGenericPartWithoutBrackets());
        } else {
            for (TypeMirror tm : ((DeclaredType) this.type).getTypeArguments()) {
                if (tm instanceof WildcardType) {
                    typeArgs.add("U" + (typeArgs.size() - 1));
                } else {
                    typeArgs.add(tm.toString());
                }
            }
        }
        return superName + "<" + Join.commaSpace(typeArgs) + ">";
    }

    public String getBindingTypeForPathWithR() {
        if (this.isForGenericTypeParameter()) {
            return this.getInnerClassDeclaration();
        }
        String bindingName = Util.lowerCaseOuterClassNames("bindgen." + this.name.getWithoutGenericPart() + "BindingPath");
        List<String> typeArgs = Copy.list("R");
        if (this.isRawType()) {
            for (int i = 0; i < this.getElement().getTypeParameters().size(); i++) {
                typeArgs.add("?");
            }
        } else if (this.name.hasGenerics()) {
            typeArgs.add(this.name.getGenericPartWithoutBrackets());
        }
        return (bindingName + "<" + Join.commaSpace(typeArgs) + ">").replaceAll(" super \\w+", ""); // for Class.getSuperClass()
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

    public boolean isRawType() {
        if (this.isFixingRawType) {
            return false;
        }
        if (this.type.getKind() == TypeKind.DECLARED) {
            return ((DeclaredType) this.type).getTypeArguments().size() != this.getElement().getTypeParameters().size();
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

    private boolean isForBinding() {
        return this.name.getWithoutGenericPart().endsWith("Binding");
    }

    private boolean isNameless() {
        return this.propertyName == null || "get".equals(this.propertyName) || "declaringClass".equals(this.propertyName);
    }

    private boolean isSkipAttributeSet() {
        String configKey = "skipAttribute." + this.enclosing.toString() + "." + this.propertyName;
        String configValue = getOption(configKey);
        return "true".equals(configValue);
    }

    private boolean isTypeParameter(Element element) {
        return element != null && element.getKind() == ElementKind.TYPE_PARAMETER;
    }

}
