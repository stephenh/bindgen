package org.exigencecorp.bindgen.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import org.apache.commons.lang.StringUtils;
import org.exigencecorp.gen.GClass;
import org.exigencecorp.gen.GMethod;

public class GenerateMethodProperty {

    private final BindingGenerator generator;
    private final GClass bindingClass;
    private final ExecutableElement enclosed;

    public GenerateMethodProperty(BindingGenerator generator, GClass bindingClass, ExecutableElement enclosed) {
        this.generator = generator;
        this.bindingClass = bindingClass;
        this.enclosed = enclosed;
    }

    public void generate() {
        String methodName = this.enclosed.getSimpleName().toString();
        String propertyName = StringUtils.uncapitalize(StringUtils.removeStart(methodName, "get"));
        TypeMirror returnType = this.enclosed.getReturnType();
        // Skip arrays for now
        if (returnType instanceof ArrayType) {
            return;
        }

        String propertyType = returnType.toString();
        String propertyGeneric = "";
        int firstBracket = propertyType.indexOf("<");
        if (firstBracket != -1) {
            propertyGeneric = propertyType.substring(firstBracket);
            propertyType = propertyType.substring(0, firstBracket);
        }
        String propertyBindingType = Massage.packageName(propertyType + "Binding" + propertyGeneric); // e.g. java.lang.StringBinding, app.EmployeeBinding, java.util.ListBinding<String>

        TypeElement propertyTypeElement = this.getProcessingEnv().getElementUtils().getTypeElement(propertyType);
        if (propertyTypeElement == null) {
            this.getProcessingEnv().getMessager().printMessage(
                Kind.ERROR,
                "No type element found for " + propertyType + " in " + this.enclosed.getEnclosingElement().getSimpleName() + "." + propertyName);
            return;
        } else {
            this.generator.generate(propertyTypeElement);
        }

        // Go back go having List<String>
        propertyType = propertyType + propertyGeneric;

        this.bindingClass.getField(propertyName).type(propertyBindingType);
        GClass fieldClass = this.bindingClass.getInnerClass("My{}Binding", StringUtils.capitalize(propertyName)).notStatic();
        fieldClass.baseClassName(propertyBindingType);

        GMethod fieldClassName = fieldClass.getMethod("getName").returnType(String.class);
        fieldClassName.body.line("return \"{}\";", propertyName);

        GMethod fieldClassGet = fieldClass.getMethod("get").returnType(propertyType);
        fieldClassGet.body.line("return {}.this.get().get{}();",//
            this.bindingClass.getSimpleClassNameWithoutGeneric(),
            StringUtils.capitalize(propertyName));

        GMethod fieldClassSet = fieldClass.getMethod("set").argument(propertyType, propertyName);
        if (this.hasSetter()) {
            fieldClassSet.body.line("{}.this.get().set{}({});", this.bindingClass.getSimpleClassNameWithoutGeneric(), StringUtils
                .capitalize(propertyName), propertyName);
        } else {
            fieldClassSet.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
        }

        GMethod fieldGet = this.bindingClass.getMethod(propertyName).returnType(propertyBindingType);
        fieldGet.body.line("if (this.{} == null) {", propertyName);
        fieldGet.body.line("    this.{} = new My{}Binding();", propertyName, StringUtils.capitalize(propertyName));
        fieldGet.body.line("}");
        fieldGet.body.line("return this.{};", propertyName);
    }

    private ProcessingEnvironment getProcessingEnv() {
        return this.generator.getProcessingEnv();
    }

    private boolean hasSetter() {
        String methodName = this.enclosed.getSimpleName().toString();
        String setterName = "set" + StringUtils.removeStart(methodName, "get");
        for (Element other : this.enclosed.getEnclosingElement().getEnclosedElements()) {
            if (other.getSimpleName().toString().equals(setterName)) {
                return true;
            }
        }
        return false;
    }

}
