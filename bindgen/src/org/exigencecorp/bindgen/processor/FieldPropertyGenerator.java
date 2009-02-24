package org.exigencecorp.bindgen.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

import org.apache.commons.lang.StringUtils;
import org.exigencecorp.bindgen.Requirements;
import org.exigencecorp.gen.GClass;
import org.exigencecorp.gen.GMethod;

public class FieldPropertyGenerator implements PropertyGenerator {

    private final BindingGenerator generator;
    private final GClass bindingClass;
    private final Element enclosed;
    private final ClassName propertyType;
    private final String propertyName;
    private TypeElement propertyTypeElement;

    public FieldPropertyGenerator(BindingGenerator generator, GClass bindingClass, Element enclosed) {
        this.generator = generator;
        this.bindingClass = bindingClass;
        this.enclosed = enclosed;
        this.propertyType = new ClassName(this.boxIfNeeded(this.enclosed.asType()));
        this.propertyName = this.enclosed.getSimpleName().toString();
    }

    public boolean shouldGenerate() {
        if (this.propertyType.getWithoutGenericPart().endsWith("Binding")) {
            return false;
        }

        if (this.shouldSkipAttribute(this.propertyName)) {
            return false;
        }

        TypeMirror fieldType = this.boxIfNeeded(this.enclosed.asType());
        this.propertyTypeElement = (TypeElement) this.getProcessingEnv().getTypeUtils().asElement(fieldType);
        if (this.propertyTypeElement == null) {
            return false;
        }

        return true;
    }

    public void generate() {
        this.bindingClass.getField(this.propertyName).type(this.propertyType.getBindingType());
        GClass fieldClass = this.bindingClass.getInnerClass("My{}Binding", StringUtils.capitalize(this.propertyName)).notStatic();
        fieldClass.baseClassName(this.propertyType.getBindingType());

        GMethod fieldClassName = fieldClass.getMethod("getName").returnType(String.class);
        fieldClassName.body.line("return \"{}\";", this.propertyName);

        GMethod fieldClassGet = fieldClass.getMethod("get").returnType(this.propertyType.get());
        fieldClassGet.body.line("return {}.this.get().{};", this.bindingClass.getSimpleClassNameWithoutGeneric(), this.propertyName);

        GMethod fieldClassSet = fieldClass.getMethod("set").argument(this.propertyType.get(), this.propertyName);
        fieldClassSet.body.line("{}.this.get().{} = {};", this.bindingClass.getSimpleClassNameWithoutGeneric(), this.propertyName, this.propertyName);

        GMethod fieldGet = this.bindingClass.getMethod(this.propertyName).returnType(this.propertyType.getBindingType());
        fieldGet.body.line("if (this.{} == null) {", this.propertyName);
        fieldGet.body.line("    this.{} = new My{}Binding();", this.propertyName, StringUtils.capitalize(this.propertyName));
        fieldGet.body.line("}");
        fieldGet.body.line("return this.{};", this.propertyName);
    }

    private ProcessingEnvironment getProcessingEnv() {
        return this.generator.getProcessingEnv();
    }

    private boolean shouldSkipAttribute(String name) {
        Requirements.skipAttributes.fulfills();
        String configKey = "skipAttribute." + this.enclosed.getEnclosingElement().toString() + "." + name;
        String configValue = this.generator.getProperties().getProperty(configKey);
        return "true".equals(configValue);
    }

    public TypeElement getPropertyTypeElement() {
        return this.propertyTypeElement;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    private TypeMirror boxIfNeeded(TypeMirror returnType) {
        if (returnType instanceof PrimitiveType) {
            return this.generator.getProcessingEnv().getTypeUtils().boxedClass((PrimitiveType) returnType).asType();
        }
        return returnType;
    }
}
