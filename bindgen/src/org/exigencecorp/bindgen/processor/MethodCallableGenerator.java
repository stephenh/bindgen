package org.exigencecorp.bindgen.processor;

import javax.lang.model.element.ExecutableElement;

import org.apache.commons.lang.StringUtils;
import org.exigencecorp.gen.GClass;
import org.exigencecorp.gen.GMethod;

public class MethodCallableGenerator {

    @SuppressWarnings("unused")
    private final BindingGenerator generator;
    private final GClass bindingClass;
    private final ExecutableElement enclosed;

    public MethodCallableGenerator(BindingGenerator generator, GClass bindingClass, ExecutableElement enclosed) {
        this.generator = generator;
        this.bindingClass = bindingClass;
        this.enclosed = enclosed;
    }

    public void generate() {
        String methodName = this.enclosed.getSimpleName().toString();

        this.bindingClass.getField(methodName).type(Runnable.class);
        GClass fieldClass = this.bindingClass.getInnerClass("My{}Binding", StringUtils.capitalize(methodName)).notStatic();
        fieldClass.implementsInterface(Runnable.class);

        GMethod fieldClassRun = fieldClass.getMethod("run");
        fieldClassRun.body.line("{}.this.get().{}();", this.bindingClass.getSimpleClassNameWithoutGeneric(), methodName);

        GMethod fieldGet = this.bindingClass.getMethod(methodName).returnType(Runnable.class);
        fieldGet.body.line("if (this.{} == null) {", methodName);
        fieldGet.body.line("    this.{} = new My{}Binding();", methodName, StringUtils.capitalize(methodName));
        fieldGet.body.line("}");
        fieldGet.body.line("return this.{};", methodName);
    }

}
