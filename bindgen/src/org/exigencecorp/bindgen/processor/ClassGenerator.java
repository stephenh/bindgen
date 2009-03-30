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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;

import org.exigencecorp.bindgen.Binding;

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
        // Put together bindingClassName, along with the generics and any bounds on them
        ClassName bindingTypeName = new ClassName(this.name.getBindingType());
        String bindingClassName = bindingTypeName.getWithoutGenericPart();
        DeclaredType dt = (DeclaredType) this.element.asType();
        if (dt.getTypeArguments().size() > 0) {
            TypeVars tv = new TypeVars(dt);
            bindingClassName += "<" + tv.genericsWithBounds + ">";
        }
        this.bindingClass = new GClass(bindingClassName);

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

        GMethod set = this.bindingClass.getMethod("set({} value)", this.name.get());
        set.body.line("this._value = value;");

        // The Binding<T> thing isn't quite working out--the set(Base) calls still need to
        // go through set(Sub) so that the inner classes that override set(Sub) to bind
        // back to actual fields and properties work for calls that end up wandering through
        // the set(Base) methods
        for (TypeElement currentElement : this.getSuperElements()) {
            GMethod setOverride = this.bindingClass.getMethod("set({} value)", new ClassName(currentElement.asType()).getWithoutGenericPart());
            setOverride.body.line("this.set(({}) value);", this.name.get());
            if (!"".equals(this.name.getGenericPart())) {
                // Causes NPEs in Eclipse
                // this.bindingClass.addAnnotation("@SuppressWarnings(\"unchecked\")");
            }
            // Causes NPEs in Eclipse
            // setOverride.addAnnotation("@Override");
        }

        GMethod get = this.bindingClass.getMethod("get()").returnType(this.name.get());
        if (this.baseElement == null) {
            get.body.line("return this._value;");
        } else {
            get.body.line("return ({}) this._value;", this.name.get());
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

        GMethod parent = this.bindingClass.getMethod("getParentBinding").returnType("Binding<?>");
        parent.body.line("return null;");

        GMethod children = this.bindingClass.getMethod("getChildBindings").returnType("List<Binding<?>>");
        children.body.line("List<Binding<?>> bindings = new java.util.ArrayList<Binding<?>>();");
        for (String foundSubBinding : this.foundSubBindings) {
            children.body.line("bindings.add(this.{}());", foundSubBinding);
        }
        for (String parentBinding : this.getBindingsOfAllSuperclasses()) {
            if (!this.foundSubBindings.contains(parentBinding)) {
                children.body.line("bindings.add(super.{}());", parentBinding);
            }
        }
        children.body.line("return bindings;");
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
            if (currentElement != null) { // javac started returning null, not sure why as Eclipse had not done that
                elements.add(currentElement);
                current = currentElement.getSuperclass();
            } else {
                current = null;
            }
        }
        return elements;
    }

    private void saveCode() {
        try {
            JavaFileObject jfo = this.getProcessingEnv().getFiler().createSourceFile(//
                this.bindingClass.getFullClassNameWithoutGeneric(),
                this.getSourceElements());
            Writer w = jfo.openWriter();
            w.write(this.bindingClass.toCode());
            w.close();
            this.queue.log("Saved " + this.bindingClass.getFullClassNameWithoutGeneric());
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
