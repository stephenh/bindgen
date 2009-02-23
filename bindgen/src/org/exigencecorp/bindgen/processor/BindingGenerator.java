package org.exigencecorp.bindgen.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

public class BindingGenerator {

    private final ProcessingEnvironment processingEnv;
    private final Properties properties = new Properties();
    private final Set<String> written = new HashSet<String>();

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

    public void generate(TypeElement element, boolean override) {
        if (!override && this.shouldIgnore(element)) {
            return;
        }
        new ClassGenerator(this, element).generate();
    }

    public ProcessingEnvironment getProcessingEnv() {
        return this.processingEnv;
    }

    private boolean shouldIgnore(TypeElement element) {
        if (element.getQualifiedName().toString().endsWith("Binding")) {
            return true;
        }

        // We recursively walk into bindings, so first check our in-memory set of what we've
        // seen so far this round. Eclipse resets our processor every time, so this only has
        // things from this round.
        if (this.written.contains(element.toString())) {
            return true;
        }

        // If we haven't seen it this round, see if we've already output awhile ago, in which
        // case we can skip it. If we really needed to do this one again, Eclipse would have
        // deleted our last output and this check wouldn't find anything.
        try {
            String[] packageAndPath = this.getBindingTypePackageAndPath(element);
            FileObject fo = this.getProcessingEnv().getFiler().getResource(
                StandardLocation.SOURCE_OUTPUT,
                packageAndPath[0],
                packageAndPath[1] + ".java");
            if (fo.getLastModified() > 0) {
                return true; // exists already
            }
        } catch (IOException io) {
        }

        // Store that we've now seen this element
        this.written.add(element.toString());

        return false;
    }

    private String[] getBindingTypePackageAndPath(TypeElement element) {
        String packageName = "";
        String pathName = new ClassName(element.asType()).getBindingType();
        // Kill generics
        int firstBracket = pathName.indexOf('<');
        if (firstBracket != -1) {
            pathName = pathName.substring(0, firstBracket);
        }
        // Find the package
        int lastDot = pathName.lastIndexOf('.');
        if (lastDot != -1) {
            packageName = pathName.substring(0, lastDot);
            pathName = pathName.substring(lastDot + 1);
        }
        return new String[] { packageName, pathName };
    }

    public Properties getProperties() {
        return this.properties;
    }

}
