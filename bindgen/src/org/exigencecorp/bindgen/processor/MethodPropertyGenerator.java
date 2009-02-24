package org.exigencecorp.bindgen.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.exigencecorp.bindgen.Requirements;
import org.exigencecorp.gen.GClass;
import org.exigencecorp.gen.GMethod;

public class MethodPropertyGenerator {

    private static final String[] javaKeywords = StringUtils
        .split(
            "abstract,continue,for,new,switch,assert,default,goto,package,synchronized,boolean,do,if,private,this,break,double,implements,protected,throw,byte,else,import,public,throws,case,enum,instanceof,return,transient,catch,extends,int,short,try,char,final,interface,static,void,class,finally,long,strictfp,volatile,const,float,native,super,while",
            ",");
    private final BindingGenerator generator;
    private final GClass bindingClass;
    private final ExecutableElement enclosed;
    private final String methodName;
    private String propertyName;
    private ClassName propertyType;
    private TypeElement propertyTypeElement;
    private boolean isFixingRawType = false;

    public MethodPropertyGenerator(BindingGenerator generator, GClass bindingClass, ExecutableElement enclosed) {
        this.generator = generator;
        this.bindingClass = bindingClass;
        this.enclosed = enclosed;
        this.methodName = this.enclosed.getSimpleName().toString();
    }

    public boolean shouldGenerate() {
        this.propertyName = this.guessPropertyNameOrNull();
        if (this.propertyName == null) {
            return false;
        }

        if (this.shouldSkipAttribute(this.propertyName)) {
            return false;
        }

        ExecutableType e = (ExecutableType) this.enclosed.asType();
        if (e.getThrownTypes().size() > 0 || e.getParameterTypes().size() > 0 || this.methodName.equals("getClass")) {
            return false;
        }

        TypeMirror returnType = this.unboxIfNeeded(this.enclosed.getReturnType());
        if (returnType instanceof ArrayType) {
            return false; // Skip arrays for now
        }

        this.propertyType = new ClassName(returnType);
        if (this.propertyType.getWithoutGenericPart().endsWith("Binding")) {
            return false; // Skip methods that themselves return bindings
        }

        this.propertyTypeElement = this.getProcessingEnv().getElementUtils().getTypeElement(this.propertyType.getWithoutGenericPart());
        if (this.propertyTypeElement == null) {
            return false;
        }

        return true;
    }

    public void generate() {
        this.fixRawTypeIfNeeded(this.propertyType, this.propertyName);

        this.bindingClass.getField(this.propertyName).type(this.propertyType.getBindingType());
        GClass fieldClass = this.bindingClass.getInnerClass("My{}Binding", StringUtils.capitalize(this.propertyName)).notStatic();
        fieldClass.baseClassName(this.propertyType.getBindingType());

        GMethod fieldClassName = fieldClass.getMethod("getName").returnType(String.class);
        fieldClassName.body.line("return \"{}\";", this.propertyName);

        GMethod fieldClassGet = fieldClass.getMethod("get").returnType(this.propertyType.get());
        fieldClassGet.body.line("return {}.this.get().{}();",//
            this.bindingClass.getSimpleClassNameWithoutGeneric(),
            this.methodName);
        if (this.isFixingRawType) {
            fieldClassGet.addAnnotation("@SuppressWarnings(\"unchecked\")");
        }

        GMethod fieldClassSet = fieldClass.getMethod("set").argument(this.propertyType.get(), this.propertyName);
        if (this.hasSetter()) {
            fieldClassSet.body.line("{}.this.get().set{}({});", this.bindingClass.getSimpleClassNameWithoutGeneric(), StringUtils
                .capitalize(this.propertyName), this.propertyName);
        } else {
            fieldClassSet.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
        }

        GMethod fieldGet = this.bindingClass.getMethod(this.propertyName).returnType(this.propertyType.getBindingType());
        fieldGet.body.line("if (this.{} == null) {", this.propertyName);
        fieldGet.body.line("    this.{} = new My{}Binding();", this.propertyName, StringUtils.capitalize(this.propertyName));
        fieldGet.body.line("}");
        fieldGet.body.line("return this.{};", this.propertyName);
    }

    private ProcessingEnvironment getProcessingEnv() {
        return this.generator.getProcessingEnv();
    }

    private boolean hasSetter() {
        String methodName = this.enclosed.getSimpleName().toString();
        String setterName = "set" + StringUtils.removeStart(methodName, this.getPrefix());
        for (Element other : this.enclosed.getEnclosingElement().getEnclosedElements()) {
            if (other.getSimpleName().toString().equals(setterName)) {
                ExecutableElement e = (ExecutableElement) other;
                return e.getThrownTypes().size() == 0; // only true if no throws
            }
        }
        return false;
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
        Requirements.fixRawTypesByAddingGenericHints.fulfills();
        String configKey = "fixRawType." + this.enclosed.getEnclosingElement().toString() + "." + propertyName;
        String configValue = this.generator.getProperties().getProperty(configKey);
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
                propertyName = StringUtils.uncapitalize(this.methodName.substring(possible.length()));
                break;
            }
        }
        // Ugly duplication from MethodPropertyGenerator
        if (ArrayUtils.contains(MethodPropertyGenerator.javaKeywords, propertyName)) {
            propertyName = this.methodName;
        }
        return propertyName;
    }

    private TypeMirror unboxIfNeeded(TypeMirror returnType) {
        if (returnType instanceof PrimitiveType) {
            return this.generator.getProcessingEnv().getTypeUtils().boxedClass((PrimitiveType) returnType).asType();
        }
        return returnType;
    }

    private boolean shouldSkipAttribute(String name) {
        Requirements.skipAttributes.fulfills();
        String configKey = "skipAttribute." + this.enclosed.getEnclosingElement().toString() + "." + name;
        String configValue = this.generator.getProperties().getProperty(configKey);
        return "true".equals(configValue);
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public TypeElement getPropertyTypeElement() {
        return this.propertyTypeElement;
    }
}
