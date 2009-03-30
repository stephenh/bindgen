package org.exigencecorp.bindgen.processor;

import java.lang.reflect.Modifier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;
import joist.util.Inflector;

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
        TypeMirror boxed = this.queue.boxIfNeededOrNull(this.enclosed.asType());
        if (boxed != null) {
            this.propertyType = new ClassName(boxed);
        } else {
            this.propertyType = null;
        }
        this.propertyName = this.enclosed.getSimpleName().toString();
        this.detectFinal();
    }

    public boolean shouldGenerate() {
        if (this.propertyType == null || this.propertyType.getWithoutGenericPart().endsWith("Binding")) {
            return false;
        }

        if (this.shouldSkipAttribute(this.propertyName) || "get".equals(this.propertyName)) {
            return false;
        }

        TypeMirror fieldType = this.queue.boxIfNeededOrNull(this.enclosed.asType());
        if (fieldType == null) {
            return false; // Skip methods we (javac) could not box appropriately
        }

        Element fieldTypeAsElement = this.getProcessingEnv().getTypeUtils().asElement(fieldType);
        // if (fieldTypeAsElement instanceof TypeParameterElement && !fieldType.toString().equals(fieldTypeAsElement.toString())) {
        if (fieldTypeAsElement instanceof TypeParameterElement) {
            this.propertyGenericElement = (TypeParameterElement) fieldTypeAsElement;
            this.propertyTypeElement = this.getProcessingEnv().getElementUtils().getTypeElement("java.lang.Object");
            this.propertyType = new ClassName("java.lang.Object");
        } else if (fieldTypeAsElement instanceof TypeElement) {
            this.propertyTypeElement = (TypeElement) fieldTypeAsElement;
        } else {
            return false;
        }

        return true;
    }

    public void generate() {
        this.bindingClass.getField(this.propertyName).type(this.propertyType.getBindingType());
        GClass fieldClass = this.bindingClass.getInnerClass("My{}Binding", Inflector.capitalize(this.propertyName)).notStatic();
        fieldClass.baseClassName(this.propertyType.getBindingType());

        GMethod fieldClassName = fieldClass.getMethod("getName").returnType(String.class);
        fieldClassName.body.line("return \"{}\";", this.propertyName);

        GMethod fieldClassGetParent = fieldClass.getMethod("getParentBinding").returnType("Binding<?>");
        fieldClassGetParent.body.line("return {}.this;", this.bindingClass.getSimpleClassNameWithoutGeneric());

        GMethod fieldClassGet = fieldClass.getMethod("get").returnType(this.propertyType.get());
        fieldClassGet.body.line("return {}.this.get().{};", this.bindingClass.getSimpleClassNameWithoutGeneric(), this.propertyName);

        GMethod fieldClassSet = fieldClass.getMethod("set").argument(this.propertyType.get(), this.propertyName);
        if (!this.isFinal && this.propertyGenericElement != null) {
            // Add SuppressWarnings when Eclipse gets fixed
            fieldClassSet.body.line(
                "{}.this.get().{} = ({}) {};",
                this.bindingClass.getSimpleClassNameWithoutGeneric(),
                this.propertyName,
                this.propertyGenericElement.toString(),
                this.propertyName);
        } else if (!this.isFinal) {
            fieldClassSet.body.line(
                "{}.this.get().{} = {};",
                this.bindingClass.getSimpleClassNameWithoutGeneric(),
                this.propertyName,
                this.propertyName);
        } else {
            fieldClassSet.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
        }

        GMethod fieldGet = this.bindingClass.getMethod(this.propertyName + "()").returnType(this.propertyType.getBindingType());
        fieldGet.body.line("if (this.{} == null) {", this.propertyName);
        fieldGet.body.line("    this.{} = new My{}Binding();", this.propertyName, Inflector.capitalize(this.propertyName));
        fieldGet.body.line("}");
        fieldGet.body.line("return this.{};", this.propertyName);
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

    private void detectFinal() {
        try {
            // Eclipse has a _binding.modifiers field we can tell if this field is final
            Object modifiers = FieldWalker.walk(this.enclosed, "_binding", "modifiers");
            if ((((Integer) modifiers).intValue() & Modifier.FINAL) != 0) {
                this.isFinal = true;
            }
        } catch (Exception e) {
            // final detection failed
        }
        try {
            // javac has a flags_field field
            Object flags = FieldWalker.walk(this.enclosed, "data", "val$env", "tree", "mods", "flags");
            if ((((Long) flags).intValue() & Modifier.FINAL) != 0) {
                this.isFinal = true;
            }
        } catch (Exception e) {
            // final detection failed
        }
    }

}
