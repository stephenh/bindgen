package org.exigencecorp.bindgen.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import org.exigencecorp.bindgen.Bindable;

@SupportedAnnotationTypes( { "org.exigencecorp.bindgen.Bindable" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BindgenAnnotationProcessor extends AbstractProcessor {

    private BindingGenerator generator;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        System.out.println("Initing");
        super.init(processingEnv);
        this.generator = new BindingGenerator(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Bindable.class)) {
            if (element instanceof PackageElement) {
                for (Element nested : ((PackageElement) element).getEnclosedElements()) {
                    this.generator.generate((TypeElement) nested);
                }
            } else if (element instanceof TypeElement) {
                System.out.println("Generating " + element);
                this.generator.generate((TypeElement) element);
            } else {
                this.processingEnv.getMessager().printMessage(Kind.WARNING, "Unhandled element " + element);
            }
        }
        System.out.println("Done bindgen processing");
        return true;
    }

}
