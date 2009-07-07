package org.exigencecorp.bindgen.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

/** Given a TypeMirror type of a field/method property, provides information about its binding outer/inner class. */
public class Property2 extends Property {

    private final String propertyName;
    private final TypeElement enclosed;
    private final TypeParameterElement genericElement;
    private final TypeElement element;

    public Property2(TypeMirror type, TypeElement enclosed, String propertyName) {
        super(type);
        this.enclosed = enclosed;
        this.propertyName = propertyName;
        this.fixRawTypeIfNeeded();

        Element element = CurrentEnv.get().getTypeUtils().asElement(type);
        if (this.isTypeParameter(element)) {
            this.genericElement = (TypeParameterElement) element;
            this.element = null;
        } else if (element instanceof TypeElement) {
            this.element = (TypeElement) element;
            this.genericElement = null;
        } else { // we get here for Arrays, maybe other things
            this.element = null;
            this.genericElement = null;
        }
    }

    public boolean isForGenericTypeParameter() {
        return this.genericElement != null;
    }

    public boolean isNameless() {
        return this.propertyName == null
            || "get".equals(this.propertyName)
            || "declaringClass".equals(this.propertyName)
            || "getClass".equals(this.propertyName)
            || (this.element == null && this.genericElement == null);
    }

    public boolean shouldSkip() {
        String configKey = "skipAttribute." + this.enclosed.toString() + "." + this.propertyName;
        String configValue = CurrentEnv.get().getOptions().get(configKey);
        return "true".equals(configValue);
    }

    public String getName() {
        return this.propertyName;
    }

    public TypeElement getElement() {
        return this.element;
    }

    public TypeParameterElement getGenericElement() {
        return this.genericElement;
    }

    private boolean isTypeParameter(Element element) {
        return element != null && element.getKind() == ElementKind.TYPE_PARAMETER;
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
    private void fixRawTypeIfNeeded() {
        String configKey = "fixRawType." + this.enclosed.toString() + "." + this.propertyName;
        String configValue = CurrentEnv.get().getOptions().get(configKey);
        if (!this.hasGenerics() && configValue != null) {
            this.name = new ClassName2(this.type.toString() + "<" + configValue + ">");
            this.isFixingRawType = true;
        }
    }

}
