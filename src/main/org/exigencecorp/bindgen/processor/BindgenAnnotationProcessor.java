package org.exigencecorp.bindgen.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import org.apache.commons.lang.StringUtils;
import org.exigencecorp.bindgen.Binding;
import org.exigencecorp.gen.GClass;
import org.exigencecorp.gen.GMethod;

@SupportedAnnotationTypes( { "org.exigencecorp.bindgen.Bindable" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BindgenAnnotationProcessor extends AbstractProcessor {

    private boolean hasWritten = false;
    private Set<TypeElement> written = new HashSet<TypeElement>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (this.hasWritten) {
            return true;
        }

        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element instanceof PackageElement) {
                    for (Element nested : ((PackageElement) element).getEnclosedElements()) {
                        this.generateBinding((TypeElement) nested);
                    }
                } else if (element instanceof TypeElement) {
                    this.generateBinding((TypeElement) element);
                } else {
                    this.processingEnv.getMessager().printMessage(Kind.WARNING, "Unhandled element " + element);
                }
            }
        }

        this.hasWritten = true;
        return true;
    }

    private void generateBinding(TypeElement element) {
        if (this.written.contains(element) || element.getSimpleName().toString().endsWith("Binding")) {
            return;
        }

        String bindingClassName = element.getSimpleName().toString() + "Binding";
        String bindingClassFullName = this.massageJavaPackageNames(element.getQualifiedName().toString() + "Binding");

        GClass gb = new GClass(bindingClassFullName);
        gb.implementsInterface(Binding.class.getName() + "<{}>", element.getSimpleName());
        gb.getField("value").type(element.getSimpleName().toString());

        GMethod set = gb.getMethod("set").argument(element.getSimpleName().toString(), "value");
        set.body.line("this.value = value;");

        GMethod get = gb.getMethod("get").returnType(element.getSimpleName().toString());
        get.body.line("return this.value;");

        GMethod name = gb.getMethod("getName").returnType(String.class);
        name.body.line("return \"\";");

        GMethod type = gb.getMethod("getType").returnType("Class<{}>", element.getSimpleName().toString());
        type.body.line("return {}.class;", element.getSimpleName().toString());

        for (Element enclosed : this.processingEnv.getElementUtils().getAllMembers(element)) {
            if (enclosed.getKind() == ElementKind.FIELD
                && enclosed.getModifiers().contains(Modifier.PUBLIC)
                && !enclosed.getModifiers().contains(Modifier.STATIC)) {
                String fieldName = enclosed.getSimpleName().toString();
                String fieldType = enclosed.asType().toString();
                String fieldBindingType = this.massageJavaPackageNames(fieldType + "Binding"); // e.g. java.lang.StringBinding, app.EmployeeBinding

                TypeElement fieldTypeElement = this.processingEnv.getElementUtils().getTypeElement(fieldType);
                if (fieldTypeElement == null) {
                    this.processingEnv.getMessager().printMessage(
                        Kind.ERROR,
                        "No type element found for " + fieldType + " in " + bindingClassFullName + "." + fieldName);
                    continue;
                }

                this.generateBinding(fieldTypeElement);

                gb.getField(fieldName).type(fieldBindingType);
                GClass fieldClass = gb.getInnerClass("My{}Binding", StringUtils.capitalize(fieldName)).notStatic();
                fieldClass.baseClassName(fieldBindingType);

                GMethod fieldClassName = fieldClass.getMethod("getName").returnType(String.class);
                fieldClassName.body.line("return \"{}\";", fieldName);

                GMethod fieldClassGet = fieldClass.getMethod("get").returnType(fieldType);
                fieldClassGet.body.line("return {}.this.get().{};", bindingClassName, fieldName);

                GMethod fieldClassSet = fieldClass.getMethod("set").argument(fieldType, fieldName);
                fieldClassSet.body.line("{}.this.get().{} = {};", bindingClassName, fieldName, fieldName);

                GMethod fieldGet = gb.getMethod(fieldName).returnType(fieldBindingType);
                fieldGet.body.line("if (this.{} == null) {", fieldName);
                fieldGet.body.line("    this.{} = new My{}Binding();", fieldName, StringUtils.capitalize(fieldName));
                fieldGet.body.line("}");
                fieldGet.body.line("return this.{};", fieldName);
            }
        }

        try {
            JavaFileObject jfo = this.processingEnv.getFiler().createSourceFile(gb.getFullClassName(), element);
            Writer w = jfo.openWriter();
            w.write(gb.toCode());
            w.close();
        } catch (IOException io) {
            this.processingEnv.getMessager().printMessage(Kind.ERROR, io.getMessage());
        }

        this.written.add(element);
    }

    private String massageJavaPackageNames(String originalBindingName) {
        if (originalBindingName.startsWith("java")) {
            return "bindgen." + originalBindingName;
        }
        return originalBindingName;
    }

}
