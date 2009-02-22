package org.exigencecorp.bindgen.processor;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
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
            InputStream is = this.getClass().getResourceAsStream("/bindgen.properties");
            this.properties.load(is);
            is.close();
        } catch (Exception e) {
            this.processingEnv.getMessager().printMessage(Kind.ERROR, "bindgen.properties failed: " + e.getMessage());
        }
        for (Map.Entry<String, String> entry : processingEnv.getOptions().entrySet()) {
            this.properties.put(entry.getKey(), entry.getValue());
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
