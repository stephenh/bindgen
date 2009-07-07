package org.exigencecorp.bindgen.processor;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;
import joist.util.Inflector;

import org.exigencecorp.bindgen.NamedBinding;

public class MethodCallableGenerator implements PropertyGenerator {

    private final GenerationQueue queue;
    private final GClass outerClass;
    private final ExecutableElement method;
    private final String methodName;
    private TypeElement blockType;
    private ExecutableElement blockMethod;

    public MethodCallableGenerator(GenerationQueue queue, GClass outerClass, ExecutableElement method) {
        this.queue = queue;
        this.outerClass = outerClass;
        this.method = method;
        this.methodName = this.method.getSimpleName().toString();
    }

    @Override
    public boolean isCallable() {
        return true;
    }

    public boolean shouldGenerate() {
        if (this.shouldSkipAttribute(this.methodName)) {
            return false;
        }
        for (String classNameToAttempt : this.getBlockTypesToAttempt()) {
            if (this.blockTypeMatchesMethod(classNameToAttempt)) {
                return true;
            }
        }
        return false;
    }

    private boolean blockTypeMatchesMethod(String attemptClassName) {
        TypeElement attemptType = this.queue.getProcessingEnv().getElementUtils().getTypeElement(attemptClassName);
        List<ExecutableElement> methods = ElementFilter.methodsIn(attemptType.getEnclosedElements());
        if (methods.size() != 1) {
            return false; // We only like classes with 1 method
        }
        ExecutableElement methodToMatch = methods.get(0);
        if (this.doBlockReturnTypesMatch(methodToMatch) //
            && this.doBlockParamsMatch(methodToMatch)
            && this.doBlockThrowsMatch(methodToMatch)) {
            this.blockType = attemptType;
            this.blockMethod = methodToMatch;
            return true;
        }
        return false;
    }

    public void generate() {
        String methodName = this.method.getSimpleName().toString();

        this.outerClass.getField(methodName).type(this.blockType.getQualifiedName().toString());
        GClass fieldClass = this.outerClass.getInnerClass("My{}Binding", Inflector.capitalize(methodName)).notStatic();
        fieldClass.implementsInterface(this.blockType.getQualifiedName().toString());
        fieldClass.implementsInterface(NamedBinding.class);

        GMethod fieldClassRun = fieldClass.getMethod(this.blockMethod.getSimpleName().toString());
        fieldClassRun.returnType(this.blockMethod.getReturnType().toString());

        // Figure out whether we need a "return" or not
        String returnPrefix = this.blockMethod.getReturnType().getKind() == TypeKind.VOID ? "" : "return ";
        String arguments = "";
        for (VariableElement foo : this.blockMethod.getParameters()) {
            arguments += foo.getSimpleName().toString() + ", ";
        }
        if (arguments.length() > 0) {
            arguments = arguments.substring(0, arguments.length() - 2); // remove last ", "
        }

        fieldClassRun.body.line("{}{}.this.get().{}({});", returnPrefix, this.outerClass.getSimpleClassNameWithoutGeneric(), methodName, arguments);
        // Add the parameters
        for (VariableElement foo : this.blockMethod.getParameters()) {
            fieldClassRun.argument(foo.asType().toString(), foo.getSimpleName().toString());
        }
        // Add throws
        for (TypeMirror type : this.method.getThrownTypes()) {
            fieldClassRun.addThrows(type.toString());
        }

        GMethod fieldClassName = fieldClass.getMethod("getName").returnType(String.class);
        fieldClassName.body.line("return \"{}\";", methodName);

        GMethod fieldGet = this.outerClass.getMethod(methodName).returnType(this.blockType.getQualifiedName().toString());
        fieldGet.body.line("if (this.{} == null) {", methodName);
        fieldGet.body.line("    this.{} = new My{}Binding();", methodName, Inflector.capitalize(methodName));
        fieldGet.body.line("}");
        fieldGet.body.line("return this.{};", methodName);
    }

    public String getPropertyName() {
        return this.methodName;
    }

    public TypeElement getPropertyTypeElement() {
        return null;
    }

    private boolean doBlockReturnTypesMatch(ExecutableElement methodToMatch) {
        return methodToMatch.getReturnType().equals(this.method.getReturnType());
    }

    private boolean doBlockParamsMatch(ExecutableElement methodToMatch) {
        if (methodToMatch.getParameters().size() != this.getMethodAsType().getParameterTypes().size()) {
            return false;
        }
        boolean allMatch = true;
        for (int i = 0; i < methodToMatch.getParameters().size(); i++) {
            if (!methodToMatch.getParameters().get(i).asType().equals(this.getMethodAsType().getParameterTypes().get(i))) {
                allMatch = false;
            }
        }
        return allMatch;
    }

    private boolean doBlockThrowsMatch(ExecutableElement methodToMatch) {
        for (TypeMirror throwsType : this.method.getThrownTypes()) {
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
        String attempts = this.queue.getProperties().getProperty("blockTypes");
        if (attempts == null) {
            attempts = "java.lang.Runnable";
        } else {
            attempts += ",java.lang.Runnable";
        }
        return attempts.split(",");
    }

    private boolean shouldSkipAttribute(String name) {
        String configKey = "skipAttribute." + this.method.getEnclosingElement().toString() + "." + name;
        String configValue = this.queue.getProperties().getProperty(configKey);
        return "true".equals(configValue);
    }

    private ExecutableType getMethodAsType() {
        return (ExecutableType) this.method.asType();
    }

}
