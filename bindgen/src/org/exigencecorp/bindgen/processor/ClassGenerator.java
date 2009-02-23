package org.exigencecorp.bindgen.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import org.apache.commons.lang.StringUtils;
import org.exigencecorp.bindgen.Binding;
import org.exigencecorp.bindgen.Requirements;
import org.exigencecorp.gen.GClass;
import org.exigencecorp.gen.GMethod;

public class ClassGenerator {

    private final BindingGenerator generator;
    private final TypeElement element;
    private final ClassName name;
    private GClass bindingClass;

    public ClassGenerator(BindingGenerator generator, TypeElement element) {
        this.generator = generator;
        this.element = element;
        this.name = new ClassName(element.asType());
    }

    public void generate() {
        this.initializeBindingClass();
        this.addConstructors();
        this.addValueGetAndSet();
        this.addNameAndType();
        this.generateProperties();
        this.saveCode();
    }

    private void initializeBindingClass() {
        this.bindingClass = new GClass(this.name.getBindingType());
        this.bindingClass.implementsInterface(Binding.class.getName() + "<{}>", this.name.get());
    }

    private void addConstructors() {
        this.bindingClass.getConstructor();
        this.bindingClass.getConstructor(this.name.get() + " value").body.line("this.set(value);");
    }

    private void addValueGetAndSet() {
        this.bindingClass.getField("value").type(this.name.get());

        GMethod set = this.bindingClass.getMethod("set").argument(this.name.get(), "value");
        set.body.line("this.value = value;");

        GMethod get = this.bindingClass.getMethod("get").returnType(this.name.get());
        get.body.line("return this.value;");
    }

    private void addNameAndType() {
        GMethod name = this.bindingClass.getMethod("getName").returnType(String.class);
        name.body.line("return \"\";");

        GMethod type = this.bindingClass.getMethod("getType").returnType("Class<?>", this.element.getSimpleName());
        type.body.line("return {}.class;", this.element.getSimpleName());
    }

    private void generateProperties() {
        for (Element enclosed : this.getProcessingEnv().getElementUtils().getAllMembers(this.element)) {
            if (!enclosed.getModifiers().contains(Modifier.PUBLIC) || enclosed.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            this.generateFieldPropertyIfNeeded(enclosed);
            this.generateMethodPropertyIfNeeded(enclosed);
            this.generateMethodCallableIfNeeded(enclosed);
        }
    }

    private void generateFieldPropertyIfNeeded(Element enclosed) {
        if (!enclosed.getKind().isField()) {
            return;
        }

        String fieldType = this.getProcessingEnv().getTypeUtils().erasure(enclosed.asType()).toString();
        if (fieldType.endsWith("Binding")) {
            return;
        }

        String fieldName = enclosed.getSimpleName().toString();
        if (this.shouldSkipAttribute(fieldName)) {
            return;
        }

        new FieldPropertyGenerator(this.generator, this.bindingClass, enclosed).generate();
    }

    private void generateMethodPropertyIfNeeded(Element enclosed) {
        if (enclosed.getKind() != ElementKind.METHOD) {
            return;
        }

        String methodName = enclosed.getSimpleName().toString();

        String propertyName = null;
        for (String possible : new String[] { "get", "to", "has", "is" }) {
            if (methodName.startsWith(possible)
                && methodName.length() > possible.length() + 1
                && methodName.substring(possible.length(), possible.length() + 1).matches("[A-Z]")) {
                propertyName = StringUtils.uncapitalize(methodName.substring(possible.length()));
                break;
            }
        }
        if (propertyName == null) {
            return;
        }

        if (this.shouldSkipAttribute(propertyName)) {
            return;
        }

        ExecutableType e = (ExecutableType) enclosed.asType();
        boolean okay = e.getThrownTypes().size() == 0
            && e.getParameterTypes().size() == 0
            && !methodName.equals("getClass")
            && !e.getReturnType().toString().endsWith("Binding");
        if (!okay) {
            return;
        }

        new MethodPropertyGenerator(this.generator, this.bindingClass, (ExecutableElement) enclosed).generate();
    }

    private void generateMethodCallableIfNeeded(Element enclosed) {
        if (enclosed.getKind() != ElementKind.METHOD) {
            return;
        }

        ExecutableType method = (ExecutableType) enclosed.asType();
        String methodName = enclosed.getSimpleName().toString();
        if (methodName.equals("wait") || methodName.equals("notify") || methodName.equals("notifyAll")) {
            return;
        }

        if (this.shouldSkipAttribute(methodName)) {
            return;
        }

        for (String attempt : this.getBlockTypesToAttempt()) {
            TypeElement attemptType = this.generator.getProcessingEnv().getElementUtils().getTypeElement(attempt);
            if (attemptType == null) {
                continue;
            }
            List<ExecutableElement> methods = ElementFilter.methodsIn(attemptType.getEnclosedElements());
            if (methods.size() != 1) {
                continue;
            }
            ExecutableElement methodToMatch = methods.get(0);
            boolean returnMatches = this.doBlockReturnTypesMatch(method, methodToMatch);
            boolean paramsMatch = this.doBlockParamsMatch(method, methodToMatch);
            boolean throwsMatch = this.doBlockThrowsMatch(method, methodToMatch);
            if (returnMatches && paramsMatch && throwsMatch) {
                new MethodCallableGenerator(this.generator, this.bindingClass, (ExecutableElement) enclosed, attemptType, methodToMatch).generate();
                return;
            }
        }
    }

    private boolean doBlockReturnTypesMatch(ExecutableType method, ExecutableElement methodToMatch) {
        return methodToMatch.getReturnType().equals(method.getReturnType());
    }

    private boolean doBlockParamsMatch(ExecutableType method, ExecutableElement methodToMatch) {
        if (methodToMatch.getParameters().size() != method.getParameterTypes().size()) {
            return false;
        }
        boolean allMatch = true;
        for (int i = 0; i < methodToMatch.getParameters().size(); i++) {
            if (!methodToMatch.getParameters().get(i).asType().equals(method.getParameterTypes().get(i))) {
                allMatch = false;
            }
        }
        return allMatch;
    }

    private boolean doBlockThrowsMatch(ExecutableType method, ExecutableElement methodToMatch) {
        for (TypeMirror throwsType : method.getThrownTypes()) {
            boolean matchesOne = false;
            for (TypeMirror otherType : methodToMatch.getThrownTypes()) {
                if (otherType.equals(throwsType)) {
                    matchesOne = true;
                }
            }
            if (!matchesOne) {
                return false;
            }
        }
        return true;
    }

    private String[] getBlockTypesToAttempt() {
        String attempts = this.generator.getProperties().getProperty("blockTypes");
        if (attempts == null) {
            attempts = "java.lang.Runnable";
        } else {
            attempts += ",java.lang.Runnable";
        }
        return StringUtils.split(attempts, ",");
    }

    private void saveCode() {
        try {
            JavaFileObject jfo = this.getProcessingEnv().getFiler().createSourceFile(//
                this.bindingClass.getFullClassNameWithoutGeneric(),
                this.element);
            Writer w = jfo.openWriter();
            w.write(this.bindingClass.toCode());
            w.close();
        } catch (IOException io) {
            this.getProcessingEnv().getMessager().printMessage(Kind.ERROR, io.getMessage());
        }
    }

    private ProcessingEnvironment getProcessingEnv() {
        return this.generator.getProcessingEnv();
    }

    private boolean shouldSkipAttribute(String name) {
        Requirements.skipAttributes.fulfills();
        String configKey = "skipAttribute." + this.element.toString() + "." + name;
        String configValue = this.generator.getProperties().getProperty(configKey);
        return "true".equals(configValue);
    }
}
