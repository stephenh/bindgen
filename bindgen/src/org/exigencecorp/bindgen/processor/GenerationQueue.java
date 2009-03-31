package org.exigencecorp.bindgen.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

public class GenerationQueue {

    private final ProcessingEnvironment processingEnv;
    private final Properties properties = new Properties();
    // From what I can tell, both Eclipse and javac will use the same processor across all of the rounds, so this should be cumulative
    private final Set<String> written = new HashSet<String>();
    private final List<TypeElement> queue = new ArrayList<TypeElement>();
    private final boolean log;
    private final boolean skipExistingBindingCheck;

    public GenerationQueue(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;

        // Default properties--this is ugly, but I could not get a bindgen.properties to be found on the classpath
        this.properties.put("fixRawType.javax.servlet.ServletConfig.initParameterNames", "String");
        this.properties.put("fixRawType.javax.servlet.ServletContext.attributeNames", "String");
        this.properties.put("fixRawType.javax.servlet.ServletContext.initParameterNames", "String");
        this.properties.put("fixRawType.javax.servlet.ServletRequest.attributeNames", "String");
        this.properties.put("fixRawType.javax.servlet.ServletRequest.parameterNames", "String");
        this.properties.put("fixRawType.javax.servlet.ServletRequest.locales", "Locale");
        this.properties.put("fixRawType.javax.servlet.ServletRequest.parameterMap", "String, String[]");
        this.properties.put("fixRawType.javax.servlet.http.HttpServletRequest.headerNames", "String");
        this.properties.put("fixRawType.javax.servlet.http.HttpSession.attributeNames", "String");
        this.properties.put("skipAttribute.javax.servlet.http.HttpSession.sessionContext", "true");
        this.properties.put("skipAttribute.javax.servlet.http.HttpServletRequest.requestedSessionIdFromUrl", "true");
        this.properties.put("skipAttribute.javax.servlet.ServletContext.servletNames", "true");
        this.properties.put("skipAttribute.javax.servlet.ServletContext.servlets", "true");
        this.properties.put("skipAttribute.java.lang.Object.getClass", "true");
        this.properties.put("skipAttribute.java.lang.Object.notify", "true");
        this.properties.put("skipAttribute.java.lang.Object.notifyAll", "true");

        // Now get the user-defined properties
        for (Map.Entry<String, String> entry : processingEnv.getOptions().entrySet()) {
            this.properties.put(entry.getKey(), entry.getValue());
        }

        this.log = "true".equals(this.properties.get("bindgen.log"));
        this.skipExistingBindingCheck = "true".equals(processingEnv.getOptions().get("bindgen.skipExistingBindingCheck"));
    }

    public void enqueueForcefully(TypeElement element) {
        this.queue.add(element);
        this.written.add(element.toString());
    }

    public void enqueueIfNew(TypeElement element) {
        // javac is lovely and was passing in nulls
        if (element != null && !this.shouldIgnore(element)) {
            this.queue.add(element);
        }
    }

    public void processQueue() {
        while (this.queue.size() != 0) {
            TypeElement element = this.queue.remove(0);
            new ClassGenerator(this, element).generate();
        }
    }

    public void updateBindKeywordClass() {
        new BindKeywordGenerator(this).generate(this.written);
    }

    public TypeMirror boxIfNeeded(TypeMirror returnType) {
        if (returnType instanceof PrimitiveType) {
            // double check--Eclipse worked fine but javac is letting non-primitive types in here
            if (returnType.toString().indexOf('.') == -1) {
                try {
                    return this.getProcessingEnv().getTypeUtils().boxedClass((PrimitiveType) returnType).asType();
                } catch (NullPointerException npe) {
                    return returnType; // it is probably a type parameter, e.g. T
                }
            }
        }
        return returnType;
    }

    public void log(String message) {
        if (this.log) {
            System.out.println(message + " in " + this);
        }
    }

    public Properties getProperties() {
        return this.properties;
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
        // things from this round. -- By "round" here, I think I mean compile-cycle, so
        // technically multiple, successive rounds.
        if (this.written.contains(element.toString())) {
            return true;
        }

        // If we haven't seen it this round, see if we've already output awhile ago, in which
        // case we can skip it. If we really needed to do this one again, Eclipse would have
        // deleted our last output and this check wouldn't find anything. But this does not
        // work for javac, so we don't do it if told not to. Would be nice to auto-detect javac.
        if (!this.skipExistingBindingCheck) {
            try {
                ClassName bindingClassName = new ClassName(new ClassName(element.asType()).getBindingType());
                FileObject fo = this.getProcessingEnv().getFiler().getResource(
                    StandardLocation.SOURCE_OUTPUT,
                    bindingClassName.getPackageName(),
                    bindingClassName.getSimpleName() + ".java");
                if (fo.getLastModified() > 0) {
                    return true; // exists already
                }
            } catch (IOException io) {
                this.processingEnv.getMessager().printMessage(Kind.ERROR, io.getMessage());
            }
        }

        // Store that we've now seen this element
        this.written.add(element.toString());

        return false;
    }

}
