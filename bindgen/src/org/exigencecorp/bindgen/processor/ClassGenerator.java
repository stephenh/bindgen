package org.exigencecorp.bindgen.processor;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Generated;
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

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;
import joist.util.Make;

import org.exigencecorp.bindgen.AbstractBinding;
import org.exigencecorp.bindgen.Binding;

public class ClassGenerator {

    private final GenerationQueue queue;
    private final TypeElement element;
    private final TypeMirror baseElement;
    private final Property name;
    private final List<String> foundSubBindings = new ArrayList<String>();
    private final List<String> done = new ArrayList<String>();
    private GClass pathBindingClass;
    private GClass rootBindingClass;

    public ClassGenerator(GenerationQueue queue, TypeElement element) {
        this.queue = queue;
        this.element = element;
        this.name = new Property(element.asType());
        this.baseElement = this.isOfTypeObjectOrNone(this.element.getSuperclass()) ? null : this.element.getSuperclass();
    }

    public void generate() {
        this.initializePathBindingClass();
        this.addGetName();
        this.addGetType();
        this.generateProperties();
        this.addGetChildBindings();

        this.initializeRootBindingClass();
        this.addConstructors();
        this.addGetWithRoot();

        this.addGeneratedTimestamp();
        this.saveCode(this.pathBindingClass);
        this.saveCode(this.rootBindingClass);
    }

    private void initializePathBindingClass() {
        this.pathBindingClass = new GClass(this.name.getBindingPathClassDeclaration());
        this.pathBindingClass.baseClassName("{}<R, {}>", AbstractBinding.class.getName(), this.name.get());
        this.pathBindingClass.setAbstract();
    }

    private void initializeRootBindingClass() {
        this.rootBindingClass = new GClass(this.name.getBindingRootClassDeclaration());
        this.rootBindingClass.baseClassName(this.name.getBindingRootClassSuperClass());
    }

    private void addGetWithRoot() {
        GMethod getWithRoot = this.rootBindingClass.getMethod("getWithRoot").argument(this.name.get(), "root").returnType(this.name.get());
        getWithRoot.body.line("return root;");
    }

    private void addGeneratedTimestamp() {
        String value = BindgenAnnotationProcessor.class.getName();
        String date = new SimpleDateFormat("dd MMM yyyy hh:mm").format(new Date());
        this.pathBindingClass.addImports(Generated.class);
        this.pathBindingClass.addAnnotation("@Generated(value = \"" + value + "\", date = \"" + date + "\")");
        this.rootBindingClass.addImports(Generated.class);
        this.rootBindingClass.addAnnotation("@Generated(value = \"" + value + "\", date = \"" + date + "\")");
    }

    private void addConstructors() {
        this.rootBindingClass.getConstructor();
        this.rootBindingClass.getConstructor(this.name.get() + " value").body.line("this.set(value);");
    }

    private void addGetName() {
        GMethod name = this.pathBindingClass.getMethod("getName").returnType(String.class).addAnnotation("@Override");
        name.body.line("return \"\";");
    }

    private void addGetType() {
        GMethod type = this.pathBindingClass.getMethod("getType").returnType("Class<?>").addAnnotation("@Override");
        type.body.line("return {}.class;", this.element.getSimpleName());
    }

    private void generateProperties() {
        for (TypeElement e : Make.list(this.element).with(this.getSuperElements())) {
            this.generatePropertiesForType(e);
        }
    }

    private void generatePropertiesForType(TypeElement element) {
        for (PropertyGenerator pg : this.getPropertyGenerators(element)) {
            if (this.doneAlreadyContainsPropertyFromSubClass(pg)) {
                continue;
            }
            pg.generate();
            this.markDone(pg);
            this.enqueuePropertyTypeIfNeeded(pg);
        }
    }

    // in case a parent class has the same field/method name as a child class
    private boolean doneAlreadyContainsPropertyFromSubClass(PropertyGenerator pg) {
        return this.done.contains(pg.getPropertyName());
    }

    private void markDone(PropertyGenerator pg) {
        this.done.add(pg.getPropertyName());
    }

    private void enqueuePropertyTypeIfNeeded(PropertyGenerator pg) {
        if (pg.isCallable()) {
            return;
        }
        this.foundSubBindings.add(pg.getPropertyName());
        this.queue.enqueueIfNew(pg.getPropertyTypeElement());
    }

    private void addGetChildBindings() {
        this.pathBindingClass.addImports(Binding.class, List.class);
        GMethod children = this.pathBindingClass.getMethod("getChildBindings").returnType("List<Binding<?>>").addAnnotation("@Override");
        children.body.line("List<Binding<?>> bindings = new java.util.ArrayList<Binding<?>>();");
        for (String foundSubBinding : this.foundSubBindings) {
            children.body.line("bindings.add(this.{}());", foundSubBinding);
        }
        children.body.line("return bindings;");
    }

    private List<TypeElement> getSuperElements() {
        List<TypeElement> elements = new ArrayList<TypeElement>();
        TypeMirror current = this.baseElement;
        while (current != null && !this.isOfTypeObjectOrNone(current)) {
            TypeElement currentElement = (TypeElement) this.getProcessingEnv().getTypeUtils().asElement(current);
            if (currentElement != null) { // javac started returning null, not sure why as Eclipse had not done that
                elements.add(currentElement);
                current = currentElement.getSuperclass();
            } else {
                current = null;
            }
        }
        return elements;
    }

    private void saveCode(GClass gc) {
        try {
            JavaFileObject jfo = this.getProcessingEnv().getFiler().createSourceFile(//
                gc.getFullClassNameWithoutGeneric(),
                this.getSourceElements());
            Writer w = jfo.openWriter();
            w.write(gc.toCode());
            w.close();
            this.queue.log("Saved " + gc.getFullClassNameWithoutGeneric());
        } catch (IOException io) {
            this.getProcessingEnv().getMessager().printMessage(Kind.ERROR, io.getMessage());
        }
    }

    private Element[] getSourceElements() {
        int i = 0;
        Element[] sourceElements = new Element[this.getSuperElements().size() + 1];
        sourceElements[i++] = this.element;
        for (TypeElement superElement : this.getSuperElements()) {
            sourceElements[i++] = superElement;
        }
        return sourceElements;
    }

    private List<PropertyGenerator> getPropertyGenerators(TypeElement type) {
        List<PropertyGenerator> generators = new ArrayList<PropertyGenerator>();
        for (Element enclosed : type.getEnclosedElements()) {
            if (!enclosed.getModifiers().contains(Modifier.PUBLIC) || enclosed.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            if (enclosed.getKind().isField()) {
                FieldPropertyGenerator fpg = new FieldPropertyGenerator(this.queue, this.pathBindingClass, enclosed);
                if (fpg.shouldGenerate()) {
                    generators.add(fpg);
                    continue;
                }
            } else if (enclosed.getKind() == ElementKind.METHOD) {
                MethodPropertyGenerator mpg = new MethodPropertyGenerator(this.queue, this.pathBindingClass, (ExecutableElement) enclosed);
                if (mpg.shouldGenerate()) {
                    generators.add(mpg);
                    continue;
                }
                MethodCallableGenerator mcg = new MethodCallableGenerator(this.queue, this.pathBindingClass, (ExecutableElement) enclosed);
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
