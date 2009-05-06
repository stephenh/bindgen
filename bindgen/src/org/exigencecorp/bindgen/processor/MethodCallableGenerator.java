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
    private final GClass bindingClass;
    private final ExecutableElement enclosed;
    private final String methodName;
    private TypeElement blockType;
    private ExecutableElement blockMethod;

    public MethodCallableGenerator(GenerationQueue queue, GClass bindingClass, ExecutableElement enclosed) {
        this.queue = queue;
        this.bindingClass = bindingClass;
        this.enclosed = enclosed;
        this.methodName = this.enclosed.getSimpleName().toString();
    }

    public boolean shouldGenerate() {
        if (this.shouldSkipAttribute(this.methodName)) {
            return false;
        }

        ExecutableType method = (ExecutableType) this.enclosed.asType();
        for (String attempt : this.getBlockTypesToAttempt()) {
            TypeElement attemptType = this.queue.getProcessingEnv().getElementUtils().getTypeElement(attempt);
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
                this.blockType = attemptType;
                this.blockMethod = methodToMatch;
                return true;
            }
        }

        return false; // none of the attempts worked
    }

    public void generate() {
        String methodName = this.enclosed.getSimpleName().toString();

        this.bindingClass.getField(methodName).type(this.blockType.getQualifiedName().toString());
        GClass fieldClass = this.bindingClass.getInnerClass("My{}Binding", Inflector.capitalize(methodName)).notStatic();
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

        fieldClassRun.body.line("{}{}.this.get().{}({});", returnPrefix, this.bindingClass.getSimpleClassNameWithoutGeneric(), methodName, arguments);
        // Add the parameters
        for (VariableElement foo : this.blockMethod.getParameters()) {
            fieldClassRun.argument(foo.asType().toString(), foo.getSimpleName().toString());
        }
        // Add throws
        for (TypeMirror type : this.enclosed.getThrownTypes()) {
            fieldClassRun.addThrows(type.toString());
        }

        GMethod fieldClassName = fieldClass.getMethod("getName").returnType(String.class);
        fieldClassName.body.line("return \"{}\";", methodName);

        GMethod fieldGet = this.bindingClass.getMethod(methodName).returnType(this.blockType.getQualifiedName().toString());
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
        String attempts = this.queue.getProperties().getProperty("blockTypes");
        if (attempts == null) {
            attempts = "java.lang.Runnable";
        } else {
            attempts += ",java.lang.Runnable";
        }
        return attempts.split(",");
    }

    private boolean shouldSkipAttribute(String name) {
        String configKey = "skipAttribute." + this.enclosed.getEnclosingElement().toString() + "." + name;
        String configValue = this.queue.getProperties().getProperty(configKey);
        return "true".equals(configValue);
    }

}
