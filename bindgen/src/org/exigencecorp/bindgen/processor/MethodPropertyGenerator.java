package org.exigencecorp.bindgen.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

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
    private boolean isFixingRawType = false;

    public MethodPropertyGenerator(BindingGenerator generator, GClass bindingClass, ExecutableElement enclosed) {
        this.generator = generator;
        this.bindingClass = bindingClass;
        this.enclosed = enclosed;
    }

    public void generate() {
        String methodName = this.enclosed.getSimpleName().toString();

        String propertyName = StringUtils.uncapitalize(StringUtils.removeStart(methodName, this.getPrefix()));
        if (ArrayUtils.contains(javaKeywords, propertyName)) {
            propertyName = methodName;
        }

        TypeMirror returnType = this.enclosed.getReturnType();
        if (returnType instanceof PrimitiveType) {
            returnType = this.generator.getProcessingEnv().getTypeUtils().boxedClass((PrimitiveType) returnType).asType();
        }
        if (returnType instanceof ArrayType) {
            return; // Skip arrays for now
        }

        ClassName propertyType = new ClassName(returnType);
        this.fixRawTypeIfNeeded(propertyType, propertyName);

        TypeElement propertyTypeElement = this.getProcessingEnv().getElementUtils().getTypeElement(propertyType.getWithoutGenericPart());
        if (propertyTypeElement == null) {
            this.getProcessingEnv().getMessager().printMessage(
                Kind.ERROR,
                "No type element found for " + propertyType + " in " + this.enclosed.getEnclosingElement().getSimpleName() + "." + propertyName);
            return;
        } else {
            this.generator.generate(propertyTypeElement);
        }

        this.bindingClass.getField(propertyName).type(propertyType.getBindingType());
        GClass fieldClass = this.bindingClass.getInnerClass("My{}Binding", StringUtils.capitalize(propertyName)).notStatic();
        fieldClass.baseClassName(propertyType.getBindingType());

        GMethod fieldClassName = fieldClass.getMethod("getName").returnType(String.class);
        fieldClassName.body.line("return \"{}\";", propertyName);

        GMethod fieldClassGet = fieldClass.getMethod("get").returnType(propertyType.get());
        fieldClassGet.body.line("return {}.this.get().{}();",//
            this.bindingClass.getSimpleClassNameWithoutGeneric(),
            methodName);
        if (this.isFixingRawType) {
            fieldClassGet.addAnnotation("@SuppressWarnings(\"unchecked\")");
        }

        GMethod fieldClassSet = fieldClass.getMethod("set").argument(propertyType.get(), propertyName);
        if (this.hasSetter()) {
            fieldClassSet.body.line("{}.this.get().set{}({});", this.bindingClass.getSimpleClassNameWithoutGeneric(), StringUtils
                .capitalize(propertyName), propertyName);
        } else {
            fieldClassSet.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
        }

        GMethod fieldGet = this.bindingClass.getMethod(propertyName).returnType(propertyType.getBindingType());
        fieldGet.body.line("if (this.{} == null) {", propertyName);
        fieldGet.body.line("    this.{} = new My{}Binding();", propertyName, StringUtils.capitalize(propertyName));
        fieldGet.body.line("}");
        fieldGet.body.line("return this.{};", propertyName);
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

}
