package org.exigencecorp.bindgen.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;
import joist.util.Inflector;

import org.exigencecorp.bindgen.AbstractBinding;

public class MethodPropertyGenerator implements PropertyGenerator {

    public static final String[] existingBindingMethods = "get,set".split(",");
    private static final String[] javaKeywords = "abstract,continue,for,new,switch,assert,default,goto,package,synchronized,boolean,do,if,private,this,break,double,implements,protected,throw,byte,else,import,public,throws,case,enum,instanceof,return,transient,catch,extends,int,short,try,char,final,interface,static,void,class,finally,long,strictfp,volatile,const,float,native,super,while"
        .split(",");
    private final GenerationQueue queue;
    private final GClass bindingClass;
    private final ExecutableElement enclosed;
    private final String methodName;
    private String propertyName;
    private ClassName propertyType;
    private TypeElement propertyTypeElement;
    private TypeParameterElement propertyGenericElement;
    private boolean isFixingRawType = false;

    public MethodPropertyGenerator(GenerationQueue queue, GClass bindingClass, ExecutableElement enclosed) {
        this.queue = queue;
        this.bindingClass = bindingClass;
        this.enclosed = enclosed;
        this.methodName = this.enclosed.getSimpleName().toString();
    }

    public boolean shouldGenerate() {
        this.propertyName = this.guessPropertyNameOrNull();
        if (this.propertyName == null) {
            return false;
        }

        if (this.shouldSkipAttribute(this.propertyName) || "get".equals(this.propertyName)) {
            return false;
        }

        ExecutableType e = (ExecutableType) this.enclosed.asType();
        if (e.getThrownTypes().size() > 0 || e.getParameterTypes().size() > 0) {
            return false;
        }

        TypeMirror returnType = this.queue.boxIfNeeded(this.enclosed.getReturnType());
        this.propertyType = new ClassName(returnType);
        if (this.propertyType.getWithoutGenericPart().endsWith("Binding")) {
            return false; // Skip methods that themselves return bindings
        }

        Element returnTypeAsElement = this.getProcessingEnv().getTypeUtils().asElement(returnType);
        if (returnTypeAsElement instanceof TypeParameterElement) {
            // javac goes in here even when this is not really a TypeParameterElement, which I thought meant it was always generic
            if (this.isReallyATypeParameter(returnTypeAsElement)) {
                this.propertyGenericElement = (TypeParameterElement) returnTypeAsElement;
                this.propertyType = new ClassName(this.propertyGenericElement.toString());
                this.propertyTypeElement = null;
            } else {
                // recover the non-parameter element for javac
                this.propertyTypeElement = this.getProcessingEnv().getElementUtils().getTypeElement(returnTypeAsElement.toString());
                this.propertyType = new ClassName(returnType.toString());
            }
        } else if (returnTypeAsElement instanceof TypeElement) {
            this.propertyTypeElement = (TypeElement) returnTypeAsElement;
        } else {
            return false;
        }

        return true;
    }

    private boolean isReallyATypeParameter(Element e) {
        DeclaredType parent = (DeclaredType) this.enclosed.getEnclosingElement().asType();
        for (TypeMirror m : parent.getTypeArguments()) {
            if (e.toString().equals(m.toString())) {
                return true;
            }
        }
        return false;
    }

    public void generate() {
        this.fixRawTypeIfNeeded(this.propertyType, this.propertyName);

        String innerClassBindingName = "My" + Inflector.capitalize(this.propertyName) + "Binding";
        this.bindingClass.getField(this.propertyName).type(innerClassBindingName);

        GClass fieldClass = this.bindingClass.getInnerClass(innerClassBindingName).notStatic();
        if (this.propertyGenericElement != null) {
            fieldClass.baseClassName("{}<{}>", AbstractBinding.class.getName(), this.propertyGenericElement);
            fieldClass.getMethod("getType").returnType("Class<?>").body.line("return null;");
        } else {
            fieldClass.baseClassName(this.propertyType.getBindingType());
        }

        GMethod fieldClassName = fieldClass.getMethod("getName").returnType(String.class);
        fieldClassName.body.line("return \"{}\";", this.propertyName);

        GMethod fieldClassGetParent = fieldClass.getMethod("getParentBinding").returnType("Binding<?>");
        fieldClassGetParent.body.line("return {}.this;", this.bindingClass.getSimpleClassNameWithoutGeneric());

        GMethod fieldClassGet = fieldClass.getMethod("get").returnType(this.propertyType.get());
        fieldClassGet.body.line("return {}.this.get().{}();", this.bindingClass.getSimpleClassNameWithoutGeneric(), this.methodName);
        if (this.isFixingRawType) {
            fieldClassGet.addAnnotation("@SuppressWarnings(\"unchecked\")");
        }

        GMethod fieldClassSet = fieldClass.getMethod("set({} {})", this.propertyType.get(), this.propertyName);
        if (this.hasSetter()) {
            fieldClassSet.body.line("{}.this.get().{}({});",//
                this.bindingClass.getSimpleClassNameWithoutGeneric(),
                this.getSetterName(),
                this.propertyName);
        } else {
            fieldClassSet.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
        }

        GMethod fieldGet = this.bindingClass.getMethod(this.propertyName + "()");
        if (this.propertyGenericElement != null) {
            fieldGet.returnType(innerClassBindingName);
        } else {
            fieldGet.returnType(this.propertyType.getBindingType());
        }
        fieldGet.body.line("if (this.{} == null) {", this.propertyName);
        fieldGet.body.line("    this.{} = new {}();", this.propertyName, innerClassBindingName);
        fieldGet.body.line("}");
        fieldGet.body.line("return this.{};", this.propertyName);
    }

    private ProcessingEnvironment getProcessingEnv() {
        return this.queue.getProcessingEnv();
    }

    private boolean hasSetter() {
        String setterName = this.getSetterName();
        for (Element other : this.enclosed.getEnclosingElement().getEnclosedElements()) {
            if (other.getSimpleName().toString().equals(setterName)) {
                ExecutableElement e = (ExecutableElement) other;
                return e.getParameters().size() == 1 && e.getThrownTypes().size() == 0; // only true if no throws
            }
        }
        return false;
    }

    private String getSetterName() {
        String methodName = this.enclosed.getSimpleName().toString();
        return "set" + methodName.substring(this.getPrefix().length());
    }

    /** Add generic suffixes to avoid warnings in bindings for pre-1.5 APIs.
     *
     * This is for old pre-1.5 APIs that use, say, Enumeration. We upgrade it
     * to something like Enumeration<String> based on the user configuration,
     * e.g.:
     *
     * <code>fixRawType.javax.servlet.http.HttpServletRequest.attributeNames=String</code>
     *
     */
    private void fixRawTypeIfNeeded(ClassName propertyType, String propertyName) {
        String configKey = "fixRawType." + this.enclosed.getEnclosingElement().toString() + "." + propertyName;
        String configValue = this.queue.getProperties().getProperty(configKey);
        if ("".equals(propertyType.getGenericPart()) && configValue != null) {
            propertyType.appendGenericType(configValue);
            this.isFixingRawType = true;
        }
    }

    private String getPrefix() {
        String methodName = this.enclosed.getSimpleName().toString();
        for (String possible : new String[] { "get", "to", "has", "is" }) {
            if (methodName.startsWith(possible)) {
                return possible;
            }
        }
        return null;
    }

    private String guessPropertyNameOrNull() {
        String propertyName = null;
        for (String possible : new String[] { "get", "to", "has", "is" }) {
            if (this.methodName.startsWith(possible)
                && this.methodName.length() > possible.length() + 1
                && this.methodName.substring(possible.length(), possible.length() + 1).matches("[A-Z]")) {
                propertyName = Inflector.uncapitalize(this.methodName.substring(possible.length()));
                break;
            }
        }
        // Ugly duplication from MethodPropertyGenerator
        boolean isKeyword = false;
        for (String keyword : javaKeywords) {
            if (keyword.equals(propertyName)) {
                isKeyword = true;
                break;
            }
        }
        if (isKeyword || "get".equals(propertyName)) {
            propertyName = this.methodName;
        }
        return propertyName;
    }

    private boolean shouldSkipAttribute(String name) {
        String configKey = "skipAttribute." + this.enclosed.getEnclosingElement().toString() + "." + name;
        String configValue = this.queue.getProperties().getProperty(configKey);
        return "true".equals(configValue);
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public TypeElement getPropertyTypeElement() {
        return this.propertyTypeElement;
    }
}
