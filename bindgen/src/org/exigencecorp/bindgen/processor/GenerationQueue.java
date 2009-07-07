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
    private final boolean skipBindKeyword;

    public GenerationQueue(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.addDefaultProperties();
        this.addProcessingEnvProperties();
        this.log = this.isEnabled("bindgen.log");
        this.skipExistingBindingCheck = this.isEnabled("bindgen.skipExistingBindingCheck");
        this.skipBindKeyword = this.isEnabled("bindgen.skipBindKeyword");
    }

    public void enqueueForcefully(TypeElement element) {
        // Even when done forcefully, we can only touch elements once/round
        if (this.hasAlreadyBeenWrittenByThisRound(element)) {
            return;
        }
        this.enqueue(element);
    }

    public void enqueueIfNew(TypeElement element) {
        if (this.hasAlreadyBeenWrittenByThisRound(element) || this.hasAlreadyBeenWrittenByAPreviousRound(element)) {
            return;
        }
        this.enqueue(element);
    }

    public void processQueue() {
        while (this.queue.size() != 0) {
            new ClassGenerator(this, this.queue.remove(0)).generate();
        }
    }

    public void updateBindKeywordClass() {
        if (this.skipBindKeyword) {
            return;
        }
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

    private void enqueue(TypeElement element) {
        this.queue.add(element);
        this.written.add(element.toString());
    }

    // We recursively walk into bindings, so first check our in-memory set of what we've
    // seen so far this round. Eclipse resets our processor every time, so this only has
    // things from this round. -- By "round" here, I think I mean compile-cycle, so
    // technically multiple, successive rounds.
    private boolean hasAlreadyBeenWrittenByThisRound(TypeElement element) {
        return this.written.contains(element.toString());
    }

    // If we haven't seen it this round, see if we've already output awhile ago, in which
    // case we can skip it. If we really needed to do this one again, Eclipse would have
    // deleted our last output and this check wouldn't find anything. But this does not
    // work for javac, so we don't do it if told not to. Would be nice to auto-detect javac.
    private boolean hasAlreadyBeenWrittenByAPreviousRound(TypeElement element) {
        if (this.skipExistingBindingCheck) {
            return false;
        }
        try {
            ClassName2 bindingClassName = new Property(element.asType()).getBindingType();
            FileObject fo = this.getProcessingEnv().getFiler().getResource(
                StandardLocation.SOURCE_OUTPUT,
                bindingClassName.getPackageName(),
                bindingClassName.getSimpleName() + ".java");
            return fo.getLastModified() > 0; // exists already
        } catch (IOException io) {
            this.processingEnv.getMessager().printMessage(Kind.ERROR, io.getMessage());
            return false;
        }
    }

    // Default properties--this is ugly, but I could not get a bindgen.properties to be found on the classpath
    private void addDefaultProperties() {
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
    }

    // Now get the user-defined properties
    private void addProcessingEnvProperties() {
        for (Map.Entry<String, String> entry : this.processingEnv.getOptions().entrySet()) {
            this.properties.put(entry.getKey(), entry.getValue());
        }
    }

    private boolean isEnabled(String property) {
        return "true".equals(this.properties.get(property));
    }

}
