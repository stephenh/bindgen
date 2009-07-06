package org.exigencecorp.bindgen.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import joist.sourcegen.GClass;
import joist.sourcegen.GField;
import joist.sourcegen.GMethod;
import joist.util.Inflector;
import joist.util.Join;

import org.exigencecorp.bindgen.AbstractBinding;
import org.exigencecorp.bindgen.ContainerBinding;

public class MethodPropertyGenerator implements PropertyGenerator {

    private final GenerationQueue queue;
    private final GClass bindingClass;
    private final ExecutableElement enclosed;
    private final String methodName;
    private String propertyName;
    private ClassName propertyType;
    private TypeElement propertyTypeElement;
    private TypeParameterElement propertyGenericElement;
    private boolean isFixingRawType = false;
    private GClass innerClass;

    public MethodPropertyGenerator(GenerationQueue queue, GClass bindingClass, ExecutableElement enclosed) {
        this.queue = queue;
        this.bindingClass = bindingClass;
        this.enclosed = enclosed;
        this.methodName = this.enclosed.getSimpleName().toString();
    }

    @Override
    public boolean isCallable() {
        return false;
    }

    public boolean shouldGenerate() {
        this.propertyName = this.guessPropertyNameOrNull();
        if (this.propertyName == null
            || this.shouldSkipAttribute(this.propertyName)
            || "get".equals(this.propertyName)
            || "declaringClass".equals(this.propertyName)
            || this.methodThrowsExceptions()
            || this.methodHasParameters()) {
            return false;
        }

        TypeMirror returnType = this.queue.boxIfNeeded(this.enclosed.getReturnType());
        this.propertyType = new ClassName(returnType);
        if (this.propertyType.getWithoutGenericPart().endsWith("Binding")) {
            return false; // Skip methods that themselves return bindings
        }

        Element returnTypeAsElement = this.getProcessingEnv().getTypeUtils().asElement(returnType);
        if (returnTypeAsElement != null && returnTypeAsElement.getKind() == ElementKind.TYPE_PARAMETER) {
            this.propertyGenericElement = (TypeParameterElement) returnTypeAsElement;
            this.propertyType = new ClassName(this.propertyGenericElement.toString());
            this.propertyTypeElement = null;
        } else if (returnTypeAsElement instanceof TypeElement) {
            this.propertyTypeElement = (TypeElement) returnTypeAsElement;
        } else {
            return false;
        }

        return true;
    }

    public void generate() {
        this.fixRawTypeIfNeeded(this.propertyType, this.propertyName);
        this.addOuterClassGet();
        this.addOuterClassBindingField();
        this.addInnerClass();
        this.addInnerClassGetName();
        this.addInnerClassParent();
        this.addInnerClassGet();
        this.addInnerClassGetWithRoot();
        this.addInnerClassSet();
        this.addInnerClassSetWithRoot();
        this.addInnerClassGetContainedTypeIfNeeded();
    }

    private String getInnerClassName() {
        String name = "My" + Inflector.capitalize(this.propertyName) + "Binding";

        List<String> dummyParams = new ArrayList<String>();
        TypeMirror returnType = this.queue.boxIfNeeded(this.enclosed.getReturnType());
        if (returnType instanceof DeclaredType) {
            DeclaredType dt = (DeclaredType) returnType;
            if (!this.isFixingRawType) {
                for (TypeMirror tm : dt.getTypeArguments()) {
                    if (tm instanceof WildcardType) {
                        dummyParams.add("U" + dummyParams.size());
                    }
                }
            } else {
                TypeElement e = (TypeElement) this.getProcessingEnv().getTypeUtils().asElement(dt);
                for (TypeParameterElement tpe : e.getTypeParameters()) {
                    dummyParams.add(tpe.toString());
                }
            }
        }
        if (dummyParams.size() > 0) {
            name += "<" + Join.commaSpace(dummyParams) + ">";
        }

        return name;
    }

    private String getSetType() {
        if (this.propertyType.hasWildcards()) {
            List<String> dummyParams = new ArrayList<String>();
            TypeMirror returnType = this.queue.boxIfNeeded(this.enclosed.getReturnType());
            if (returnType instanceof DeclaredType) {
                DeclaredType dt = (DeclaredType) returnType;
                for (TypeMirror tm : dt.getTypeArguments()) {
                    if (tm instanceof WildcardType) {
                        dummyParams.add("U" + (dummyParams.size()));
                    } else {
                        dummyParams.add(tm.toString());
                    }
                }
            }
            return this.propertyType.getWithoutGenericPart() + "<" + Join.commaSpace(dummyParams) + ">";
        }
        return this.propertyType.get();
    }

    private void addOuterClassGet() {
        GMethod fieldGet = this.bindingClass.getMethod(this.propertyName + "()");
        if (this.propertyGenericElement != null) {
            fieldGet.returnType(this.getInnerClassName());
        } else {
            fieldGet.returnType(this.propertyType.getBindingTypeForPathWithR());
        }

        String name = "My" + Inflector.capitalize(this.propertyName) + "Binding";
        List<String> dummyParams = new ArrayList<String>();
        TypeMirror returnType = this.queue.boxIfNeeded(this.enclosed.getReturnType());
        if (returnType instanceof DeclaredType) {
            DeclaredType dt = (DeclaredType) returnType;
            for (TypeMirror tm : dt.getTypeArguments()) {
                if (tm instanceof WildcardType) {
                    dummyParams.add("Object");
                }
            }
        }
        if (dummyParams.size() > 0) {
            name += "<" + Join.commaSpace(dummyParams) + ">";
        }

        fieldGet.body.line("if (this.{} == null) {", this.propertyName);
        fieldGet.body.line("    this.{} = new {}();", this.propertyName, name);
        fieldGet.body.line("}");
        fieldGet.body.line("return this.{};", this.propertyName);

        if (this.isFixingRawType) {
            fieldGet.addAnnotation("@SuppressWarnings(\"unchecked\")");
        }
    }

    private void addOuterClassBindingField() {
        String name = "My" + Inflector.capitalize(this.propertyName) + "Binding";

        List<String> dummyParams = new ArrayList<String>();
        TypeMirror returnType = this.queue.boxIfNeeded(this.enclosed.getReturnType());
        if (returnType instanceof DeclaredType) {
            DeclaredType dt = (DeclaredType) returnType;
            for (TypeMirror tm : dt.getTypeArguments()) {
                if (tm instanceof WildcardType) {
                    dummyParams.add("?");
                }
            }
        }
        if (dummyParams.size() > 0) {
            name += "<" + Join.commaSpace(dummyParams) + ">";
        }

        GField f = this.bindingClass.getField(this.propertyName).type(name);
        if (this.isFixingRawType) {
            f.addAnnotation("@SuppressWarnings(\"unchecked\")");
        }
    }

    private void addInnerClass() {
        this.innerClass = this.bindingClass.getInnerClass(this.getInnerClassName()).notStatic();
        if (this.propertyGenericElement != null) {
            this.innerClass.baseClassName("{}<R, {}>", AbstractBinding.class.getName(), this.propertyGenericElement);
            this.innerClass.getMethod("getType").returnType("Class<?>").body.line("return null;");
        } else {
            String superName = "bindgen." + this.propertyType.getWithoutGenericPart() + "BindingPath";

            Pattern outerClassName = Pattern.compile("\\.([A-Z]\\w+)\\.");
            Matcher m = outerClassName.matcher(superName);
            while (m.find()) {
                superName = m.replaceFirst("." + Inflector.uncapitalize(m.group(1)) + ".");
                m = outerClassName.matcher(superName);
            }

            if (this.propertyType.hasGenerics()) {
                List<String> dummyParams = new ArrayList<String>();
                dummyParams.add("R");
                TypeMirror returnType = this.queue.boxIfNeeded(this.enclosed.getReturnType());
                if (!this.isFixingRawType) {
                    DeclaredType dt = (DeclaredType) returnType;
                    for (TypeMirror tm : dt.getTypeArguments()) {
                        if (tm instanceof WildcardType) {
                            dummyParams.add("U" + (dummyParams.size() - 1));
                        } else {
                            dummyParams.add(tm.toString());
                        }
                    }
                } else {
                    dummyParams.add(this.propertyType.getGenericPartWithoutBrackets());
                }
                superName += "<" + Join.commaSpace(dummyParams) + ">";
            } else {
                superName += "<R>";
            }

            this.innerClass.baseClassName(superName);
            if (this.propertyType.hasWildcards()) {
                this.innerClass.addAnnotation("@SuppressWarnings(\"unchecked\")");
            }
        }
    }

    private void addInnerClassGetName() {
        GMethod fieldClassName = this.innerClass.getMethod("getName").returnType(String.class).addAnnotation("@Override");
        fieldClassName.body.line("return \"{}\";", this.propertyName);
    }

    private void addInnerClassParent() {
        GMethod fieldClassGetParent = this.innerClass.getMethod("getParentBinding").returnType("Binding<?>").addAnnotation("@Override");
        fieldClassGetParent.body.line("return {}.this;", this.bindingClass.getSimpleClassNameWithoutGeneric());
    }

    private void addInnerClassGet() {
        GMethod fieldClassGet = this.innerClass.getMethod("get").returnType(this.getSetType()).addAnnotation("@Override");
        if (this.propertyType.hasWildcards()) {
            fieldClassGet.body.line(
                "return ({}) {}.this.get().{}();",
                this.getSetType(),
                this.bindingClass.getSimpleClassNameWithoutGeneric(),
                this.methodName);
        } else {
            fieldClassGet.body.line("return {}.this.get().{}();", this.bindingClass.getSimpleClassNameWithoutGeneric(), this.methodName);
        }
        if (this.isFixingRawType) {
            fieldClassGet.addAnnotation("@SuppressWarnings(\"unchecked\")");
        }
    }

    private void addInnerClassGetWithRoot() {
        GMethod fieldClassGetWithRoot = this.innerClass.getMethod("getWithRoot").argument("R", "root").returnType(this.getSetType()).addAnnotation(
            "@Override");
        if (this.propertyType.hasWildcards()) {
            fieldClassGetWithRoot.body.line("return ({}) {}.this.getWithRoot(root).{}();",//
                this.getSetType(),
                this.bindingClass.getSimpleClassNameWithoutGeneric(),
                this.methodName);
        } else {
            fieldClassGetWithRoot.body.line(
                "return {}.this.getWithRoot(root).{}();",
                this.bindingClass.getSimpleClassNameWithoutGeneric(),
                this.methodName);
        }
        if (this.isFixingRawType) {
            fieldClassGetWithRoot.addAnnotation("@SuppressWarnings(\"unchecked\")");
        }
    }

    private void addInnerClassSet() {
        GMethod fieldClassSet = this.innerClass.getMethod("set({} {})", this.getSetType(), this.propertyName); // .addAnnotation("@Override");
        if (this.hasSetter()) {
            fieldClassSet.body.line("{}.this.get().{}({});",//
                this.bindingClass.getSimpleClassNameWithoutGeneric(),
                this.getSetterName(),
                this.propertyName);
        } else {
            fieldClassSet.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
        }
    }

    private void addInnerClassSetWithRoot() {
        GMethod fieldClassSetWithRoot = this.innerClass.getMethod("setWithRoot(R root, {} {})", this.getSetType(), this.propertyName);
        // .addAnnotation("@Override");
        if (this.hasSetter()) {
            fieldClassSetWithRoot.body.line("{}.this.getWithRoot(root).{}({});",//
                this.bindingClass.getSimpleClassNameWithoutGeneric(),
                this.getSetterName(),
                this.propertyName);
        } else {
            fieldClassSetWithRoot.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
        }
    }

    private void addInnerClassGetContainedTypeIfNeeded() {
        if ("java.util.List".equals(this.propertyType.getWithoutGenericPart()) || "java.util.Set".equals(this.propertyType.getWithoutGenericPart())) {
            String contained = this.propertyType.getGenericPartWithoutBrackets();
            if (!this.matchesTypeParameterOfParent(contained)) {
                this.innerClass.implementsInterface(ContainerBinding.class);
                GMethod containedType = this.innerClass.getMethod("getContainedType").returnType("Class<?>").addAnnotation("@Override");
                containedType.body.line("return {}.class;", contained);
            }
        }
    }

    private boolean matchesTypeParameterOfParent(String type) {
        if (this.propertyType.hasWildcards()) {
            return true;
        }
        for (TypeParameterElement e : ((TypeElement) this.enclosed.getEnclosingElement()).getTypeParameters()) {
            if (e.toString().equals(type)) {
                return true;
            }
        }
        return false;
    }

    private ProcessingEnvironment getProcessingEnv() {
        return this.queue.getProcessingEnv();
    }

    private boolean hasSetter() {
        String setterName = this.getSetterName();
        for (Element other : this.enclosed.getEnclosingElement().getEnclosedElements()) {
            if (other.getSimpleName().toString().equals(setterName) && other.getModifiers().contains(Modifier.PUBLIC)) {
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
        if (JavaKeywords.is(propertyName) || "get".equals(propertyName)) {
            propertyName = this.methodName;
        }
        return propertyName;
    }

    private boolean methodThrowsExceptions() {
        return ((ExecutableType) this.enclosed.asType()).getThrownTypes().size() > 0;
    }

    private boolean methodHasParameters() {
        return ((ExecutableType) this.enclosed.asType()).getParameterTypes().size() > 0;
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
