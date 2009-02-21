package org.exigencecorp.bindgen.processor;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

public class BindingGenerator {

    private final ProcessingEnvironment processingEnv;
    private final Set<TypeElement> written = new HashSet<TypeElement>();

    public BindingGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public void generate(TypeElement element) {
        if (this.shouldIgnore(element)) {
            return;
        }
        new GenerateBindingClass(this, element).generate();
    }

    public ProcessingEnvironment getProcessingEnv() {
        return this.processingEnv;
    }

    private boolean shouldIgnore(TypeElement element) {
        if (this.written.contains(element) || element.getSimpleName().toString().endsWith("Binding")) {
            return true;
        }
        this.written.add(element);
        return false;
    }

}
