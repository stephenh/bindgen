package org.exigencecorp.bindgen.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import org.apache.commons.lang.StringUtils;
import org.exigencecorp.gen.GClass;
import org.exigencecorp.gen.GMethod;

public class GenerateFieldProperty {

    private final BindingGenerator generator;
    private final GClass bindingClass;
    private final Element enclosed;

    public GenerateFieldProperty(BindingGenerator generator, GClass bindingClass, Element enclosed) {
        this.generator = generator;
        this.bindingClass = bindingClass;
        this.enclosed = enclosed;
    }

    public void generate() {
        String fieldName = this.enclosed.getSimpleName().toString();
        String fieldType = this.getProcessingEnv().getTypeUtils().erasure(this.enclosed.asType()).toString();
        // e.g. bindgen.java.lang.StringBinding, bindgen.app.EmployeeBinding
        String fieldBindingType = Massage.packageName(fieldType + "Binding");

        // Probably need to use ClassName like GenerateMethodProperty to not cause warnings on public fields with generics
        TypeElement fieldTypeElement = this.getProcessingEnv().getElementUtils().getTypeElement(fieldType);
        if (fieldTypeElement == null) {
            this.getProcessingEnv().getMessager().printMessage(
                Kind.ERROR,
                "No type element found for " + fieldType + " in " + this.bindingClass.getFullClassName() + "." + fieldName);
            return;
        }

        this.generator.generate(fieldTypeElement);

        this.bindingClass.getField(fieldName).type(fieldBindingType);
        GClass fieldClass = this.bindingClass.getInnerClass("My{}Binding", StringUtils.capitalize(fieldName)).notStatic();
        fieldClass.baseClassName(fieldBindingType);

        GMethod fieldClassName = fieldClass.getMethod("getName").returnType(String.class);
        fieldClassName.body.line("return \"{}\";", fieldName);

        GMethod fieldClassGet = fieldClass.getMethod("get").returnType(fieldType);
        fieldClassGet.body.line("return {}.this.get().{};", this.bindingClass.getSimpleClassNameWithoutGeneric(), fieldName);

        GMethod fieldClassSet = fieldClass.getMethod("set").argument(fieldType, fieldName);
        fieldClassSet.body.line("{}.this.get().{} = {};", this.bindingClass.getSimpleClassNameWithoutGeneric(), fieldName, fieldName);

        GMethod fieldGet = this.bindingClass.getMethod(fieldName).returnType(fieldBindingType);
        fieldGet.body.line("if (this.{} == null) {", fieldName);
        fieldGet.body.line("    this.{} = new My{}Binding();", fieldName, StringUtils.capitalize(fieldName));
        fieldGet.body.line("}");
        fieldGet.body.line("return this.{};", fieldName);
    }

    private ProcessingEnvironment getProcessingEnv() {
        return this.generator.getProcessingEnv();
    }

}
