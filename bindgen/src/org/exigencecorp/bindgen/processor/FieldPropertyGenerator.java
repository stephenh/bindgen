package org.exigencecorp.bindgen.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;
import joist.util.Inflector;

import org.exigencecorp.bindgen.AbstractBinding;
import org.exigencecorp.bindgen.ContainerBinding;

public class FieldPropertyGenerator implements PropertyGenerator {

    private final GenerationQueue queue;
    private final GClass bindingClass;
    private final Element enclosed;
    private final String propertyName;
    private ClassName propertyType;
    private TypeElement propertyTypeElement;
    private TypeParameterElement propertyGenericElement;
    private boolean isFinal = false;

    public FieldPropertyGenerator(GenerationQueue queue, GClass bindingClass, Element enclosed) {
        this.queue = queue;
        this.bindingClass = bindingClass;
        this.enclosed = enclosed;
        this.propertyType = new ClassName(this.queue.boxIfNeeded(this.enclosed.asType()));
        this.propertyName = this.enclosed.getSimpleName().toString();
        this.isFinal = this.enclosed.getModifiers().contains(javax.lang.model.element.Modifier.FINAL);
    }

    public boolean shouldGenerate() {
        if (this.propertyType.getWithoutGenericPart().endsWith("Binding")) {
            return false;
        }

        if (this.shouldSkipAttribute(this.propertyName) || "get".equals(this.propertyName)) {
            return false;
        }

        TypeMirror fieldType = this.queue.boxIfNeeded(this.enclosed.asType());
        if (fieldType == null) {
            return false; // Skip methods we (javac) could not box appropriately
        }

        Element fieldTypeAsElement = this.getProcessingEnv().getTypeUtils().asElement(fieldType);
        if (fieldTypeAsElement instanceof TypeParameterElement) {
            // javac goes in here even when this is not really a TypeParameterElement, which I thought meant it was always generic
            if (this.isReallyATypeParameter(fieldTypeAsElement)) {
                this.propertyGenericElement = (TypeParameterElement) fieldTypeAsElement;
                this.propertyType = new ClassName(this.propertyGenericElement.toString());
                this.propertyTypeElement = null;
            } else {
                // recover the non-parameter element for javac
                this.propertyTypeElement = this.getProcessingEnv().getElementUtils().getTypeElement(fieldTypeAsElement.toString());
                this.propertyType = new ClassName(fieldType.toString());
            }
        } else if (fieldTypeAsElement instanceof TypeElement) {
            this.propertyTypeElement = (TypeElement) fieldTypeAsElement;
        } else {
            return false;
        }

        return true;
    }

    private boolean isReallyATypeParameter(Element e) {
        DeclaredType parent = (DeclaredType) this.enclosed.getEnclosingElement().asType();
        for (TypeMirror m : parent.getTypeArguments()) {
            if (e.toString().equals(m.toString())) {
                return true;
            }
        }
        return false;
    }

    public void generate() {
        String innerClassBindingName = "My" + Inflector.capitalize(this.propertyName) + "Binding";
        this.bindingClass.getField(this.propertyName).type(innerClassBindingName);

        GClass fieldClass = this.bindingClass.getInnerClass(innerClassBindingName).notStatic();
        if (this.propertyGenericElement != null) {
            fieldClass.baseClassName("{}<{}>", AbstractBinding.class.getName(), this.propertyGenericElement);
            fieldClass.getMethod("getType").returnType("Class<?>").body.line("return null;");
        } else {
            fieldClass.baseClassName(this.propertyType.getBindingType());
            if (this.propertyType.hasWildcards()) {
                fieldClass.addAnnotation("@SuppressWarnings(\"unchecked\")");
            }
        }

        GMethod fieldClassName = fieldClass.getMethod("getName").returnType(String.class);
        fieldClassName.body.line("return \"{}\";", this.propertyName);

        GMethod fieldClassGetParent = fieldClass.getMethod("getParentBinding").returnType("Binding<?>");
        fieldClassGetParent.body.line("return {}.this;", this.bindingClass.getSimpleClassNameWithoutGeneric());

        GMethod fieldClassGet = fieldClass.getMethod("get").returnType(this.propertyType.get());
        fieldClassGet.body.line("return {}.this.get().{};", this.bindingClass.getSimpleClassNameWithoutGeneric(), this.propertyName);

        GMethod fieldClassSet = fieldClass.getMethod("set").argument(this.propertyType.get(), this.propertyName);
        if (!this.isFinal) {
            fieldClassSet.body.line(
                "{}.this.get().{} = {};",
                this.bindingClass.getSimpleClassNameWithoutGeneric(),
                this.propertyName,
                this.propertyName);
        } else {
            fieldClassSet.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
        }

        GMethod fieldGet = this.bindingClass.getMethod(this.propertyName + "()");
        if (this.propertyGenericElement != null) {
            fieldGet.returnType(innerClassBindingName);
        } else {
            fieldGet.returnType(this.propertyType.getBindingType());
            if (this.propertyType.hasWildcards()) {
                fieldGet.addAnnotation("@SuppressWarnings(\"unchecked\")");
            }
        }
        fieldGet.body.line("if (this.{} == null) {", this.propertyName);
        fieldGet.body.line("    this.{} = new My{}Binding();", this.propertyName, Inflector.capitalize(this.propertyName));
        fieldGet.body.line("}");
        fieldGet.body.line("return this.{};", this.propertyName);

        if ("java.util.List".equals(this.propertyType.getWithoutGenericPart()) || "java.util.Set".equals(this.propertyType.getWithoutGenericPart())) {
            String contained = this.propertyType.getGenericPartWithoutBrackets();
            if (!this.matchesTypeParameterOfParent(contained)) {
                fieldClass.implementsInterface(ContainerBinding.class);
                GMethod containedType = fieldClass.getMethod("getContainedType").returnType("Class<?>");
                containedType.body.line("return {}.class;", contained);
            }
        }
    }

    private boolean matchesTypeParameterOfParent(String type) {
        if (this.propertyType.hasWildcards()) {
            return true;
        }
        for (TypeParameterElement e : ((TypeElement) this.enclosed.getEnclosingElement()).getTypeParameters()) {
            if (e.toString().equals(type)) {
                return true;
            }
        }
        return false;
    }

    private ProcessingEnvironment getProcessingEnv() {
        return this.queue.getProcessingEnv();
    }

    private boolean shouldSkipAttribute(String name) {
        String configKey = "skipAttribute." + this.enclosed.getEnclosingElement().toString() + "." + name;
        String configValue = this.queue.getProperties().getProperty(configKey);
        return "true".equals(configValue);
    }

    public TypeElement getPropertyTypeElement() {
        return this.propertyTypeElement;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

}
