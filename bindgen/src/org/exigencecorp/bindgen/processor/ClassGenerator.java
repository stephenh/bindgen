package org.exigencecorp.bindgen.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import org.exigencecorp.bindgen.Binding;
import org.exigencecorp.gen.GClass;
import org.exigencecorp.gen.GMethod;

public class ClassGenerator {

    private final BindingGenerator generator;
    private final TypeElement element;
    private final ClassName name;
    private final List<String> foundSubBindings = new ArrayList<String>();
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
        this.addBindingsMethod();
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
            this.generateFieldPropertyIfNeeded(enclosed);
            this.generateMethodPropertyIfNeeded(enclosed);
            this.generateMethodCallableIfNeeded(enclosed);
        }
    }

    private void generateFieldPropertyIfNeeded(Element enclosed) {
        if (!enclosed.getKind().isField()) {
            return;
        }
        FieldPropertyGenerator g = new FieldPropertyGenerator(this.generator, this.bindingClass, enclosed);
        if (g.shouldGenerate()) {
            g.generate();
            this.foundSubBindings.add(g.getPropertyName());
            this.generator.generate(g.getPropertyTypeElement(), false);
        }
    }

    private void generateMethodPropertyIfNeeded(Element enclosed) {
        if (enclosed.getKind() != ElementKind.METHOD) {
            return;
        }
        MethodPropertyGenerator g = new MethodPropertyGenerator(this.generator, this.bindingClass, (ExecutableElement) enclosed);
        if (g.shouldGenerate()) {
            g.generate();
            this.foundSubBindings.add(g.getPropertyName());
            this.generator.generate(g.getPropertyTypeElement(), false);
        }
    }

    private void generateMethodCallableIfNeeded(Element enclosed) {
        if (enclosed.getKind() != ElementKind.METHOD) {
            return;
        }
        MethodCallableGenerator g = new MethodCallableGenerator(this.generator, this.bindingClass, (ExecutableElement) enclosed);
        if (g.shouldGenerate()) {
            g.generate();
        }
    }

    private void addBindingsMethod() {
        this.bindingClass.addImports(List.class);
        GMethod bindings = this.bindingClass.getMethod("getBindings").returnType("List<Binding<?>>");
        bindings.body.line("List<Binding<?>> bindings = new java.util.ArrayList<Binding<?>>();");
        for (String foundSubBinding : this.foundSubBindings) {
            bindings.body.line("bindings.add(this.{}());", foundSubBinding);
        }
        bindings.body.line("return bindings;");
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

}
