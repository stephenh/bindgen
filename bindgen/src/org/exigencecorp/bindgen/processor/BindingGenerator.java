package org.exigencecorp.bindgen.processor;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

public class BindingGenerator {

    private final ProcessingEnvironment processingEnv;
    private final Set<TypeElement> written = new HashSet<TypeElement>();
    private final Properties properties = new Properties();

    public BindingGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        try {
            this.properties.load(this.getClass().getResourceAsStream("/bindgen.properties"));
        } catch (Exception e) {
            this.processingEnv.getMessager().printMessage(Kind.ERROR, "bindgen.properties failed: " + e.getMessage());
        }
    }

    public void generate(TypeElement element) {
        if (this.shouldIgnore(element)) {
            return;
        }
        new ClassGenerator(this, element).generate();
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

    public Properties getProperties() {
        return this.properties;
    }

}
