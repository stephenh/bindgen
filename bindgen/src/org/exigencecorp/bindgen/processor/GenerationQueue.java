package org.exigencecorp.bindgen.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

import org.exigencecorp.gen.GClass;
import org.exigencecorp.gen.GMethod;
import org.exigencecorp.util.Copy;
import org.exigencecorp.util.Join;

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

        // Now get the user-defined properties
        for (Map.Entry<String, String> entry : processingEnv.getOptions().entrySet()) {
            this.properties.put(entry.getKey(), entry.getValue());
        }

        this.log = "true".equals(this.properties.get("bindgen.log"));
        this.skipExistingBindingCheck = "true".equals(processingEnv.getOptions().get("skipExistingBindingCheck"));
    }

    public void enqueueForcefully(TypeElement element) {
        this.queue.add(element);
        this.written.add(element.toString());
    }

    public void enqueueIfNew(TypeElement element) {
        if (!this.shouldIgnore(element)) {
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
        Set<String> keywords = this.readExistingBindKeywordFile();
        keywords.addAll(this.written);

        GClass bindClass = new GClass("bindgen.BindKeyword");
        for (String className : keywords) {
            DeclaredType t = (DeclaredType) this.processingEnv.getElementUtils().getTypeElement(className).asType();
            String bindingType = new ClassName(className).getBindingType();
            if (t.getTypeArguments().size() > 0) {
                String generics = Join.commaSpace(t.getTypeArguments());
                GMethod method = bindClass
                    .getMethod("bind({}<{}> o)", className, generics)
                    .returnType("{}<{}>", bindingType, generics)
                    .typeParameters(generics)
                    .setStatic();
                method.body.line("return new {}<{}>(o);", bindingType, generics);
            } else {
                GMethod method = bindClass.getMethod("bind({} o)", className).returnType(bindingType).setStatic();
                method.body.line("return new {}(o);", bindingType);
            }
        }

        try {
            JavaFileObject jfo = this.processingEnv.getFiler().createSourceFile(bindClass.getFullClassNameWithoutGeneric());
            Writer w = jfo.openWriter();
            w.write(bindClass.toCode());
            w.close();
        } catch (IOException io) {
            this.processingEnv.getMessager().printMessage(Kind.ERROR, io.getMessage());
        }

        this.writeBindKeywordFile(keywords);
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

    private Set<String> readExistingBindKeywordFile() {
        Set<String> cache = new LinkedHashSet<String>();
        try {
            this.log("READING BindKeyword.txt");
            FileObject fo = this.processingEnv.getFiler().getResource(StandardLocation.SOURCE_OUTPUT, "bindgen", "BindKeyword.txt");
            if (fo.getLastModified() > 0) {
                String line;
                BufferedReader input = new BufferedReader(new InputStreamReader(fo.openInputStream()));
                while ((line = input.readLine()) != null) {
                    cache.add(line);
                }
                input.close();
                this.log("WAS THERE");
            } else {
                this.log("NOT THERE");
            }
        } catch (IOException io) {
            this.processingEnv.getMessager().printMessage(Kind.ERROR, io.getMessage());
        }
        return cache;
    }

    private void writeBindKeywordFile(Set<String> keywords) {
        try {
            this.log("WRITING BindKeyword.txt");
            List<String> sorted = Copy.list(keywords);
            Collections.sort(sorted);
            FileObject fo = this.processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "bindgen", "BindKeyword.txt");
            OutputStream output = fo.openOutputStream();
            for (String className : sorted) {
                output.write(className.getBytes());
                output.write("\n".getBytes());
            }
            output.close();
        } catch (IOException io) {
            this.processingEnv.getMessager().printMessage(Kind.ERROR, io.getMessage());
        }
    }

    public TypeMirror boxIfNeededOrNull(TypeMirror returnType) {
        if (returnType instanceof PrimitiveType) {
            // double check--Eclipse worked fine but javac is letting non-primitive types in here
            if (returnType.toString().indexOf('.') == -1) {
                try {
                    return this.getProcessingEnv().getTypeUtils().boxedClass((PrimitiveType) returnType).asType();
                } catch (NullPointerException npe) {
                    this.log("boxedClass failed for " + returnType);
                    return null; // bug in javac
                }
            }
        }
        return returnType;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void log(String message) {
        if (this.log) {
            System.out.println(message + " in " + this);
        }
    }

}
