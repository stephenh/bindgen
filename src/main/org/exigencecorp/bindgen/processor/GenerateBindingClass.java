package org.exigencecorp.bindgen.processor;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import org.exigencecorp.bindgen.Binding;
import org.exigencecorp.gen.GClass;
import org.exigencecorp.gen.GMethod;

public class GenerateBindingClass {

    private final BindingGenerator generator;
    private final TypeElement element;
    private GClass bindingClass;

    public GenerateBindingClass(BindingGenerator generator, TypeElement element) {
        this.generator = generator;
        this.element = element;
    }

    public void generate() {
        this.initializeBindingClass();
        this.addConstructors();
        this.addValueGetAndSet();
        this.addNameAndType();
        this.generateProperties();
        this.saveCode();
    }

    private void initializeBindingClass() {
        this.bindingClass = new GClass(Massage.packageName(this.element.getQualifiedName().toString() + "Binding"));
        this.bindingClass.implementsInterface(Binding.class.getName() + "<{}>", this.element.getQualifiedName());
    }

    private void addConstructors() {
        this.bindingClass.getConstructor();
        this.bindingClass.getConstructor(this.element.getSimpleName() + " value").body.line("this.set(value);");
    }

    private void addValueGetAndSet() {
        this.bindingClass.getField("value").type(this.element.getSimpleName().toString());

        GMethod set = this.bindingClass.getMethod("set").argument(this.element.getSimpleName().toString(), "value");
        set.body.line("this.value = value;");

        GMethod get = this.bindingClass.getMethod("get").returnType(this.element.getSimpleName().toString());
        get.body.line("return this.value;");
    }

    private void addNameAndType() {
        GMethod name = this.bindingClass.getMethod("getName").returnType(String.class);
        name.body.line("return \"\";");

        GMethod type = this.bindingClass.getMethod("getType").returnType("Class<{}>", this.element.getSimpleName().toString());
        type.body.line("return {}.class;", this.element.getSimpleName().toString());
    }

    private void generateProperties() {
        for (Element enclosed : this.getProcessingEnv().getElementUtils().getAllMembers(this.element)) {
            if (enclosed.getModifiers().contains(Modifier.PUBLIC) && !enclosed.getModifiers().contains(Modifier.STATIC)) {
                if (enclosed.getKind() == ElementKind.FIELD) {
                    new GenerateFieldProperty(this.generator, this.bindingClass, enclosed).generate();
                } else if (enclosed.getKind() == ElementKind.METHOD && this.isMethodProperty(enclosed)) {
                    new GenerateMethodProperty(this.generator, this.bindingClass, (ExecutableElement) enclosed).generate();
                } else if (enclosed.getKind() == ElementKind.METHOD && this.isMethodCallable(enclosed)) {
                    new GenerateMethodCallable(this.generator, this.bindingClass, (ExecutableElement) enclosed).generate();
                }
            }
        }
    }

    private boolean isMethodProperty(Element enclosed) {
        String methodName = enclosed.getSimpleName().toString();
        return methodName.startsWith("get") && ((ExecutableType) enclosed.asType()).getParameterTypes().size() == 0 && !methodName.equals("getClass");
    }

    private boolean isMethodCallable(Element enclosed) {
        String methodName = enclosed.getSimpleName().toString();
        ExecutableType e = (ExecutableType) enclosed.asType();
        return e.getParameterTypes().size() == 0
            && e.getReturnType().getKind() == TypeKind.VOID
            && !methodName.equals("wait")
            && !methodName.equals("notify")
            && !methodName.equals("notifyAll");
    }

    private void saveCode() {
        try {
            JavaFileObject jfo = this.getProcessingEnv().getFiler().createSourceFile(this.bindingClass.getFullClassName(), this.element);
            Writer w = jfo.openWriter();
            w.write(this.bindingClass.toCode());
            w.close();
        } catch (IOException io) {
            this.getProcessingEnv().getMessager().printMessage(Kind.ERROR, io.getMessage());
        }
    }

    private ProcessingEnvironment getProcessingEnv() {
        return this.generator.getProcessingEnv();
    }

}
