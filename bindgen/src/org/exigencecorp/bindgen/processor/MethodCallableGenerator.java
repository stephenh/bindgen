package org.exigencecorp.bindgen.processor;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.apache.commons.lang.StringUtils;
import org.exigencecorp.gen.GClass;
import org.exigencecorp.gen.GMethod;

public class MethodCallableGenerator {

    @SuppressWarnings("unused")
    private final BindingGenerator generator;
    private final GClass bindingClass;
    private final ExecutableElement enclosed;
    private final TypeElement blockType;
    private final ExecutableElement blockMethod;

    public MethodCallableGenerator(
        BindingGenerator generator,
        GClass bindingClass,
        ExecutableElement enclosed,
        TypeElement blockType,
        ExecutableElement blockMethod) {
        this.generator = generator;
        this.bindingClass = bindingClass;
        this.enclosed = enclosed;
        this.blockType = blockType;
        this.blockMethod = blockMethod;
    }

    public void generate() {
        String methodName = this.enclosed.getSimpleName().toString();

        this.bindingClass.getField(methodName).type(this.blockType.getQualifiedName().toString());
        GClass fieldClass = this.bindingClass.getInnerClass("My{}Binding", StringUtils.capitalize(methodName)).notStatic();
        fieldClass.implementsInterface(this.blockType.getQualifiedName().toString());

        GMethod fieldClassRun = fieldClass.getMethod(this.blockMethod.getSimpleName().toString());
        fieldClassRun.returnType(this.blockMethod.getReturnType().toString());

        // Figure out whether we need a "return" or not
        String returnPrefix = this.blockMethod.getReturnType().getKind() == TypeKind.VOID ? "" : "return ";
        List<String> argumentNames = new ArrayList<String>();
        for (VariableElement foo : this.blockMethod.getParameters()) {
            argumentNames.add(foo.getSimpleName().toString());
        }
        String arguments = StringUtils.join(argumentNames, ", ");

        fieldClassRun.body.line("{}{}.this.get().{}({});", returnPrefix, this.bindingClass.getSimpleClassNameWithoutGeneric(), methodName, arguments);
        // Add the parameters
        for (VariableElement foo : this.blockMethod.getParameters()) {
            fieldClassRun.argument(foo.asType().toString(), foo.getSimpleName().toString());
        }
        // Add throws
        for (TypeMirror type : this.enclosed.getThrownTypes()) {
            fieldClassRun.addThrows(type.toString());
        }

        GMethod fieldGet = this.bindingClass.getMethod(methodName).returnType(this.blockType.getQualifiedName().toString());
        fieldGet.body.line("if (this.{} == null) {", methodName);
        fieldGet.body.line("    this.{} = new My{}Binding();", methodName, StringUtils.capitalize(methodName));
        fieldGet.body.line("}");
        fieldGet.body.line("return this.{};", methodName);
    }

}
