package org.exigencecorp.bindgen.processor;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import joist.sourcegen.GClass;
import joist.sourcegen.GField;
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
    private Property propertyType;
    private TypeElement propertyTypeElement;
    private TypeParameterElement propertyGenericElement;
    private boolean isFinal = false;
    private GClass innerClass;

    public FieldPropertyGenerator(GenerationQueue queue, GClass bindingClass, Element enclosed) {
        this.queue = queue;
        this.bindingClass = bindingClass;
        this.enclosed = enclosed;
        this.propertyType = new Property(this.queue.boxIfNeeded(this.enclosed.asType()));
        this.propertyName = this.enclosed.getSimpleName().toString();
        this.isFinal = this.enclosed.getModifiers().contains(javax.lang.model.element.Modifier.FINAL);
    }

    @Override
    public boolean isCallable() {
        return false;
    }

    public boolean shouldGenerate() {
        if (this.propertyType.isForBinding() || this.shouldSkipAttribute(this.propertyName) || "get".equals(this.propertyName)) {
            return false;
        }

        TypeMirror fieldType = this.queue.boxIfNeeded(this.enclosed.asType());
        if (fieldType == null) {
            return false; // Skip methods we (javac) could not box appropriately
        }

        Element fieldTypeAsElement = this.getProcessingEnv().getTypeUtils().asElement(fieldType);
        if (fieldTypeAsElement != null && fieldTypeAsElement.getKind() == ElementKind.TYPE_PARAMETER) {
            this.propertyGenericElement = (TypeParameterElement) fieldTypeAsElement;
            this.propertyType = new Property(this.propertyGenericElement.asType());
            this.propertyTypeElement = null;
        } else if (fieldTypeAsElement instanceof TypeElement) {
            this.propertyTypeElement = (TypeElement) fieldTypeAsElement;
        } else {
            return false;
        }

        this.propertyType.fixRawTypeIfNeeded((TypeElement) this.enclosed.getEnclosingElement(), this.propertyName);

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

        TypeMirror returnType = this.enclosed.asType();

        if (returnType.getKind() == TypeKind.DECLARED) {
            List<String> dummyParams = new ArrayList<String>();
            DeclaredType dt = (DeclaredType) returnType;
            for (TypeMirror tm : dt.getTypeArguments()) {
                if (tm instanceof WildcardType) {
                    dummyParams.add("U" + dummyParams.size());
                }
            }
            if (dummyParams.size() > 0) {
                name += "<" + Join.commaSpace(dummyParams) + ">";
            }
        }

        return name;
    }

    private void addOuterClassBindingField() {
        GField f = this.bindingClass.getField(this.propertyName).type(this.propertyType.getBindingClassFieldDeclaration(this.propertyName));
        if (this.propertyType.isRawType()) {
            f.addAnnotation("@SuppressWarnings(\"unchecked\")");
        }
    }

    private void addOuterClassGet() {
        GMethod fieldGet = this.bindingClass.getMethod(this.propertyName + "()");
        if (this.propertyGenericElement != null) {
            fieldGet.returnType(this.getInnerClassName());
        } else {
            fieldGet.returnType(this.propertyType.getBindingTypeForPathWithR());
        }
        fieldGet.body.line("if (this.{} == null) {", this.propertyName);
        fieldGet.body.line("    this.{} = new {}();", this.propertyName, this.propertyType.getBindingRootClassInstantiation(this.propertyName));
        fieldGet.body.line("}");
        fieldGet.body.line("return this.{};", this.propertyName);
        if (this.propertyType.isRawType()) {
            fieldGet.addAnnotation("@SuppressWarnings(\"unchecked\")");
        }
    }

    private void addInnerClass() {
        this.innerClass = this.bindingClass.getInnerClass(this.propertyType.getInnerClass(this.propertyName)).notStatic();
        if (this.propertyGenericElement != null) {
            this.innerClass.baseClassName("{}<R, {}>", AbstractBinding.class.getName(), this.propertyGenericElement);
            this.innerClass.getMethod("getType").returnType("Class<?>").body.line("return null;");
        } else {
            this.innerClass.baseClassName(this.propertyType.getInnerClassSuperClass());
            if (this.propertyType.hasWildcards() || this.propertyType.isRawType()) {
                this.innerClass.addAnnotation("@SuppressWarnings(\"unchecked\")");
            }
        }
    }

    private void addInnerClassGetName() {
        GMethod getName = this.innerClass.getMethod("getName").returnType(String.class).addAnnotation("@Override");
        getName.body.line("return \"{}\";", this.propertyName);
    }

    private void addInnerClassGetParent() {
        GMethod getParent = this.innerClass.getMethod("getParentBinding").returnType("Binding<?>").addAnnotation("@Override");
        getParent.body.line("return {}.this;", this.bindingClass.getSimpleClassNameWithoutGeneric());
    }

    private void addInnerClassGet() {
        GMethod get = this.innerClass.getMethod("get").returnType(this.propertyType.getSetType()).addAnnotation("@Override");
        get.body.line("return {}{}.this.get().{};",//
            this.propertyType.getCastForReturnIfNeeded(),
            this.bindingClass.getSimpleClassNameWithoutGeneric(),
            this.propertyName);
        if (this.propertyType.isFixingRawType) {
            get.addAnnotation("@SuppressWarnings(\"unchecked\")");
        }
    }

    private void addInnerClassGetWithRoot() {
        GMethod getWithRoot = this.innerClass.getMethod("getWithRoot");
        getWithRoot.argument("R", "root").returnType(this.propertyType.getSetType()).addAnnotation("@Override");
        getWithRoot.body.line("return {}{}.this.getWithRoot(root).{};",//
            this.propertyType.getCastForReturnIfNeeded(),
            this.bindingClass.getSimpleClassNameWithoutGeneric(),
            this.propertyName);
        if (this.propertyType.isFixingRawType) {
            getWithRoot.addAnnotation("@SuppressWarnings(\"unchecked\")");
        }
    }

    private void addInnerClassSet() {
        GMethod set = this.innerClass.getMethod("set").argument(this.propertyType.getSetType(), this.propertyName);
        if (this.isFinal) {
            set.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
            return;
        }
        set.body.line("{}.this.get().{} = {};",//
            this.bindingClass.getSimpleClassNameWithoutGeneric(),
            this.propertyName,
            this.propertyName);
    }

    private void addInnerClassSetWithRoot() {
        GMethod setWithRoot = this.innerClass.getMethod("setWithRoot(R root, {} {})", this.propertyType.getSetType(), this.propertyName);
        if (this.isFinal) {
            setWithRoot.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
            return;
        }
        setWithRoot.body.line(
            "{}.this.getWithRoot(root).{} = {};",
            this.bindingClass.getSimpleClassNameWithoutGeneric(),
            this.propertyName,
            this.propertyName);
    }

    private void addInnerClassGetContainedTypeIfNeeded() {
        if (this.propertyType.isForListOrSet()) {
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
