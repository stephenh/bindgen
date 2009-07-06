package org.exigencecorp.bindgen.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;
import joist.util.Inflector;
import joist.util.Join;

import org.exigencecorp.bindgen.AbstractBinding;
import org.exigencecorp.bindgen.ContainerBinding;

public class FieldPropertyGenerator implements PropertyGenerator {

    private final GenerationQueue queue;
    private final GClass bindingClass;
    private final Element enclosed;
    private final String propertyName;
    private ClassName propertyType;
    private TypeElement propertyTypeElement;
    private TypeParameterElement propertyGenericElement;
    private boolean isFinal = false;
    private GClass innerClass;

    public FieldPropertyGenerator(GenerationQueue queue, GClass bindingClass, Element enclosed) {
        this.queue = queue;
        this.bindingClass = bindingClass;
        this.enclosed = enclosed;
        this.propertyType = new ClassName(this.queue.boxIfNeeded(this.enclosed.asType()));
        this.propertyName = this.enclosed.getSimpleName().toString();
        this.isFinal = this.enclosed.getModifiers().contains(javax.lang.model.element.Modifier.FINAL);
    }

    @Override
    public boolean isCallable() {
        return false;
    }

    public boolean shouldGenerate() {
        if (this.propertyType.getWithoutGenericPart().endsWith("Binding")) {
            return false;
        }

        if (this.shouldSkipAttribute(this.propertyName) || "get".equals(this.propertyName)) {
            return false;
        }

        TypeMirror fieldType = this.queue.boxIfNeeded(this.enclosed.asType());
        if (fieldType == null) {
            return false; // Skip methods we (javac) could not box appropriately
        }

        Element fieldTypeAsElement = this.getProcessingEnv().getTypeUtils().asElement(fieldType);
        if (fieldTypeAsElement != null && fieldTypeAsElement.getKind() == ElementKind.TYPE_PARAMETER) {
            this.propertyGenericElement = (TypeParameterElement) fieldTypeAsElement;
            this.propertyType = new ClassName(this.propertyGenericElement.toString());
            this.propertyTypeElement = null;
        } else if (fieldTypeAsElement instanceof TypeElement) {
            this.propertyTypeElement = (TypeElement) fieldTypeAsElement;
        } else {
            return false;
        }

        return true;
    }

    public void generate() {
        this.addOuterClassGet();
        this.addOuterClassBindingField();
        this.addInnerClass();
        this.addInnerClassGetName();
        this.addInnerClassGetParent();
        this.addInnerClassGet();
        this.addInnerClassGetWithRoot();
        this.addInnerClassSet();
        this.addInnerClassSetWithRoot();
        this.addInnerClassGetContainedTypeIfNeeded();
    }

    private String getInnerClassName() {
        String name = "My" + Inflector.capitalize(this.propertyName) + "Binding";

        List<String> dummyParams = new ArrayList<String>();
        TypeMirror returnType = this.queue.boxIfNeeded(this.enclosed.asType());
        if (returnType instanceof DeclaredType) {
            DeclaredType dt = (DeclaredType) returnType;
            for (TypeMirror tm : dt.getTypeArguments()) {
                if (tm instanceof WildcardType) {
                    dummyParams.add("U" + dummyParams.size());
                }
            }
        }
        if (dummyParams.size() > 0) {
            name += "<" + Join.commaSpace(dummyParams) + ">";
        }

        return name;
    }

    private void addOuterClassBindingField() {
        String name = "My" + Inflector.capitalize(this.propertyName) + "Binding";

        List<String> dummyParams = new ArrayList<String>();
        TypeMirror returnType = this.queue.boxIfNeeded(this.enclosed.asType());
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

        this.bindingClass.getField(this.propertyName).type(name);
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
        TypeMirror returnType = this.queue.boxIfNeeded(this.enclosed.asType());
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
                TypeMirror returnType = this.queue.boxIfNeeded(this.enclosed.asType());
                if (returnType instanceof DeclaredType) {
                    DeclaredType dt = (DeclaredType) returnType;
                    for (TypeMirror tm : dt.getTypeArguments()) {
                        if (tm instanceof WildcardType) {
                            dummyParams.add("U" + (dummyParams.size() - 1));
                        } else {
                            dummyParams.add(tm.toString());
                        }
                    }
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

    private void addInnerClassGetParent() {
        GMethod fieldClassGetParent = this.innerClass.getMethod("getParentBinding").returnType("Binding<?>").addAnnotation("@Override");
        fieldClassGetParent.body.line("return {}.this;", this.bindingClass.getSimpleClassNameWithoutGeneric());
    }

    private void addInnerClassGet() {
        GMethod fieldClassGet = this.innerClass.getMethod("get").returnType(this.getSetType()).addAnnotation("@Override");
        if (this.propertyType.hasWildcards()) {
            fieldClassGet.body.line(
                "return ({}) {}.this.get().{};",
                this.getSetType(),
                this.bindingClass.getSimpleClassNameWithoutGeneric(),
                this.propertyName);
        } else {
            fieldClassGet.body.line("return {}.this.get().{};", this.bindingClass.getSimpleClassNameWithoutGeneric(), this.propertyName);
        }
    }

    private String getSetType() {
        if (this.propertyType.hasWildcards()) {
            List<String> dummyParams = new ArrayList<String>();
            TypeMirror returnType = this.queue.boxIfNeeded(this.enclosed.asType());
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

    private void addInnerClassGetWithRoot() {
        GMethod fieldClassGetWithRoot = this.innerClass.getMethod("getWithRoot").argument("R", "root").returnType(this.getSetType()).addAnnotation(
            "@Override");
        if (this.propertyType.hasWildcards()) {
            fieldClassGetWithRoot.body.line("return ({}) {}.this.getWithRoot(root).{};",//
                this.getSetType(),
                this.bindingClass.getSimpleClassNameWithoutGeneric(),
                this.propertyName);
        } else {
            fieldClassGetWithRoot.body.line(
                "return {}.this.getWithRoot(root).{};",
                this.bindingClass.getSimpleClassNameWithoutGeneric(),
                this.propertyName);
        }
    }

    private void addInnerClassSet() {
        GMethod fieldClassSet = this.innerClass.getMethod("set").argument(this.getSetType(), this.propertyName);
        if (!this.isFinal) {
            fieldClassSet.body.line(
                "{}.this.get().{} = {};",
                this.bindingClass.getSimpleClassNameWithoutGeneric(),
                this.propertyName,
                this.propertyName);
        } else {
            fieldClassSet.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
        }
    }

    private void addInnerClassSetWithRoot() {
        GMethod fieldClassSetWithRoot = this.innerClass.getMethod("setWithRoot(R root, {} {})", this.getSetType(), this.propertyName);
        if (!this.isFinal) {
            fieldClassSetWithRoot.body.line(
                "{}.this.getWithRoot(root).{} = {};",
                this.bindingClass.getSimpleClassNameWithoutGeneric(),
                this.propertyName,
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

    private boolean shouldSkipAttribute(String name) {
        String configKey = "skipAttribute." + this.enclosed.getEnclosingElement().toString() + "." + name;
        String configValue = this.queue.getProperties().getProperty(configKey);
        return "true".equals(configValue);
    }

    public TypeElement getPropertyTypeElement() {
        return this.propertyTypeElement;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

}
