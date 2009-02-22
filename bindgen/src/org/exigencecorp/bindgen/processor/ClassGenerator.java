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

import org.apache.commons.lang.StringUtils;
import org.exigencecorp.bindgen.Binding;
import org.exigencecorp.bindgen.Requirements;
import org.exigencecorp.gen.GClass;
import org.exigencecorp.gen.GMethod;

public class ClassGenerator {

    private final BindingGenerator generator;
    private final TypeElement element;
    private final ClassName name;
    private GClass bindingClass;

    public ClassGenerator(BindingGenerator generator, TypeElement element) {
        this.generator = generator;
        this.element = element;
        this.name = new ClassName(element.asType());
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
        this.bindingClass = new GClass(this.name.getBindingType());
        this.bindingClass.implementsInterface(Binding.class.getName() + "<{}>", this.name.get());
    }

    private void addConstructors() {
        this.bindingClass.getConstructor();
        this.bindingClass.getConstructor(this.name.get() + " value").body.line("this.set(value);");
    }

    private void addValueGetAndSet() {
        this.bindingClass.getField("value").type(this.name.get());

        GMethod set = this.bindingClass.getMethod("set").argument(this.name.get(), "value");
        set.body.line("this.value = value;");

        GMethod get = this.bindingClass.getMethod("get").returnType(this.name.get());
        get.body.line("return this.value;");
    }

    private void addNameAndType() {
        GMethod name = this.bindingClass.getMethod("getName").returnType(String.class);
        name.body.line("return \"\";");

        GMethod type = this.bindingClass.getMethod("getType").returnType("Class<?>", this.element.getSimpleName());
        type.body.line("return {}.class;", this.element.getSimpleName());
    }

    private void generateProperties() {
        for (Element enclosed : this.getProcessingEnv().getElementUtils().getAllMembers(this.element)) {
            if (!enclosed.getModifiers().contains(Modifier.PUBLIC) || enclosed.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            if (this.isInterestingFieldProperty(enclosed)) {
                new FieldPropertyGenerator(this.generator, this.bindingClass, enclosed).generate();
            } else if (this.isInterestingMethodProperty(enclosed)) {
                new MethodPropertyGenerator(this.generator, this.bindingClass, (ExecutableElement) enclosed).generate();
            } else if (this.isInterestingMethodCallable(enclosed)) {
                new MethodCallableGenerator(this.generator, this.bindingClass, (ExecutableElement) enclosed).generate();
            }
        }
    }

    private boolean isInterestingFieldProperty(Element enclosed) {
        if (!enclosed.getKind().isField()) {
            return false;
        }
        String fieldType = this.getProcessingEnv().getTypeUtils().erasure(enclosed.asType()).toString();
        if (fieldType.endsWith("Binding")) {
            return false;
        }

        String fieldName = enclosed.getSimpleName().toString();
        if (this.shouldSkipAttribute(fieldName)) {
            return false;
        }

        return true;
    }

    private boolean isInterestingMethodProperty(Element enclosed) {
        if (enclosed.getKind() != ElementKind.METHOD) {
            return false;
        }
        String methodName = enclosed.getSimpleName().toString();
        ExecutableType e = (ExecutableType) enclosed.asType();

        boolean okay = methodName.startsWith("get")
            && e.getThrownTypes().size() == 0
            && e.getParameterTypes().size() == 0
            && !methodName.equals("getClass")
            && !e.getReturnType().toString().endsWith("Binding");
        if (!okay) {
            return false;
        }

        String propertyName = StringUtils.uncapitalize(methodName.substring(3));
        if (this.shouldSkipAttribute(propertyName)) {
            return false;
        }

        return true;
    }

    private boolean isInterestingMethodCallable(Element enclosed) {
        if (enclosed.getKind() != ElementKind.METHOD) {
            return false;
        }
        String methodName = enclosed.getSimpleName().toString();
        ExecutableType e = (ExecutableType) enclosed.asType();

        boolean okay = e.getParameterTypes().size() == 0
            && e.getReturnType().getKind() == TypeKind.VOID
            && e.getThrownTypes().size() == 0
            && !methodName.equals("wait")
            && !methodName.equals("notify")
            && !methodName.equals("notifyAll");
        if (!okay) {
            return false;
        }

        if (this.shouldSkipAttribute(methodName)) {
            return false;
        }

        return true;
    }

    private void saveCode() {
        try {
            JavaFileObject jfo = this.getProcessingEnv().getFiler().createSourceFile(//
                this.bindingClass.getFullClassNameWithoutGeneric(),
                this.element);
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

    private boolean shouldSkipAttribute(String name) {
        Requirements.skipAttributes.fulfills();
        String configKey = "skipAttribute." + this.element.toString() + "." + name;
        String configValue = this.generator.getProperties().getProperty(configKey);
        return "true".equals(configValue);
    }
}
