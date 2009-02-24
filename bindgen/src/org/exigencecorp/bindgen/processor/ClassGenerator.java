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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import org.exigencecorp.bindgen.Binding;
import org.exigencecorp.gen.GClass;
import org.exigencecorp.gen.GMethod;

public class ClassGenerator {

    private final GenerationQueue queue;
    private final TypeElement element;
    private final TypeMirror baseElement;
    private final ClassName name;
    private final List<String> foundSubBindings = new ArrayList<String>();
    private GClass bindingClass;

    public ClassGenerator(GenerationQueue queue, TypeElement element) {
        this.queue = queue;
        this.element = element;
        this.name = new ClassName(element.asType());
        this.baseElement = this.isOfTypeObjectOrNone(this.element.getSuperclass()) ? null : this.element.getSuperclass();
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
        if (this.baseElement != null) {
            ClassName baseClassName = new ClassName(this.baseElement);
            this.bindingClass.baseClassName(baseClassName.getBindingType());
            this.queue.enqueueIfNew((TypeElement) this.queue.getProcessingEnv().getTypeUtils().asElement(this.baseElement));
        } else {
            this.bindingClass.implementsInterface(Binding.class.getName() + "<{}>", this.name.get());
        }
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
        for (PropertyGenerator pg : this.getPropertyGenerators(this.element)) {
            pg.generate();
            if (pg.getPropertyName() != null) {
                this.foundSubBindings.add(pg.getPropertyName());
                this.queue.enqueueIfNew(pg.getPropertyTypeElement());
            }
        }
    }

    private void addBindingsMethod() {
        this.bindingClass.addImports(Binding.class, List.class);
        GMethod bindings = this.bindingClass.getMethod("getBindings").returnType("List<Binding<?>>");
        bindings.body.line("List<Binding<?>> bindings = new java.util.ArrayList<Binding<?>>();");
        for (String foundSubBinding : this.foundSubBindings) {
            bindings.body.line("bindings.add(this.{}());", foundSubBinding);
        }
        for (String parentBinding : this.getBindingsOfAllSuperclasses()) {
            if (!this.foundSubBindings.contains(parentBinding)) {
                bindings.body.line("bindings.add(super.{}());", parentBinding);
            }
        }
        bindings.body.line("return bindings;");
    }

    private List<String> getBindingsOfAllSuperclasses() {
        List<String> names = new ArrayList<String>();
        TypeMirror current = this.baseElement;
        while (current != null) {
            TypeElement currentElement = (TypeElement) this.queue.getProcessingEnv().getTypeUtils().asElement(current);
            for (PropertyGenerator pg : this.getPropertyGenerators(currentElement)) {
                if (pg.getPropertyName() != null) {
                    names.add(pg.getPropertyName());
                }
            }
            current = this.isOfTypeObjectOrNone(currentElement.getSuperclass()) ? null : currentElement.getSuperclass();
        }
        return names;
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

    private List<PropertyGenerator> getPropertyGenerators(TypeElement type) {
        List<PropertyGenerator> generators = new ArrayList<PropertyGenerator>();
        for (Element enclosed : type.getEnclosedElements()) {
            if (!enclosed.getModifiers().contains(Modifier.PUBLIC) || enclosed.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            if (enclosed.getKind().isField()) {
                FieldPropertyGenerator fpg = new FieldPropertyGenerator(this.queue, this.bindingClass, enclosed);
                if (fpg.shouldGenerate()) {
                    generators.add(fpg);
                    continue;
                }
            } else if (enclosed.getKind() == ElementKind.METHOD) {
                MethodPropertyGenerator mpg = new MethodPropertyGenerator(this.queue, this.bindingClass, (ExecutableElement) enclosed);
                if (mpg.shouldGenerate()) {
                    generators.add(mpg);
                    continue;
                }
                MethodCallableGenerator mcg = new MethodCallableGenerator(this.queue, this.bindingClass, (ExecutableElement) enclosed);
                if (mcg.shouldGenerate()) {
                    generators.add(mcg);
                    continue;
                }
            }
        }
        return generators;
    }

    private boolean isOfTypeObjectOrNone(TypeMirror type) {
        return type.toString().equals("java.lang.Object") || type.getKind() == TypeKind.NONE;
    }

    private ProcessingEnvironment getProcessingEnv() {
        return this.queue.getProcessingEnv();
    }

}
