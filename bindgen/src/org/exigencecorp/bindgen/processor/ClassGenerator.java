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

        // this.bindingClass.addImports(Generated.class);
        // SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm");
        // this.bindingClass.addAnnotation("@Generated(value = \"" + BindgenAnnotationProcessor.class.getName() + "\", date = \"" + sdf.format(new Date()) + "\")");
    }

    private void addConstructors() {
        this.bindingClass.getConstructor();
        this.bindingClass.getConstructor(this.name.get() + " value").body.line("this.set(value);");
    }

    private void addValueGetAndSet() {
        if (this.baseElement == null) {
            this.bindingClass.getField("_value").type(this.name.get()).setProtected();
        }

        GMethod set = this.bindingClass.getMethod("set").argument(this.name.get(), "value");
        set.body.line("this._value = value;");

        // The Binding<T> thing isn't quite working out--the set(Base) calls still need to
        // go through set(Sub) so that the inner classes that override set(Sub) to bind
        // back to actual fields and properties work for calls that end up wandering through
        // the set(Base) methods
        for (TypeElement currentElement : this.getSuperElements()) {
            GMethod setOverride = this.bindingClass.getMethod("set({} value)", currentElement.toString(), "value");
            setOverride.body.line("this.set(({}) value);", this.name.get());
            // setOverride.addAnnotation("@Override");
        }

        GMethod get = this.bindingClass.getMethod("get").returnType(this.name.get());
        if (this.baseElement == null) {
            get.body.line("return this._value;");
        } else {
            get.body.line("return ({}) this._value;", this.element.toString());
        }
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
        for (TypeElement currentElement : this.getSuperElements()) {
            for (PropertyGenerator pg : this.getPropertyGenerators(currentElement)) {
                if (pg.getPropertyName() != null) {
                    names.add(pg.getPropertyName());
                }
            }
        }
        return names;
    }

    private List<TypeElement> getSuperElements() {
        List<TypeElement> elements = new ArrayList<TypeElement>();
        TypeMirror current = this.baseElement;
        while (current != null && !this.isOfTypeObjectOrNone(current)) {
            TypeElement currentElement = (TypeElement) this.getProcessingEnv().getTypeUtils().asElement(current);
            elements.add(currentElement);
            current = currentElement.getSuperclass();
        }
        return elements;
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
