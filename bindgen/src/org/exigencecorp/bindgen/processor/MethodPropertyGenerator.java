package org.exigencecorp.bindgen.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import joist.sourcegen.GClass;
import joist.sourcegen.GField;
import joist.sourcegen.GMethod;
import joist.util.Inflector;

import org.exigencecorp.bindgen.AbstractBinding;
import org.exigencecorp.bindgen.ContainerBinding;

public class MethodPropertyGenerator implements PropertyGenerator {

    private final GenerationQueue queue;
    private final GClass bindingClass;
    private final ExecutableElement enclosed;
    private final String methodName;
    private String propertyName;
    private Property propertyType;
    private TypeElement propertyTypeElement;
    private TypeParameterElement propertyGenericElement;
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
        this.propertyType = new Property(returnType);
        if (this.propertyType.getWithoutGenericPart().endsWith("Binding")) {
            return false; // Skip methods that themselves return bindings
        }

        Element returnTypeAsElement = this.getProcessingEnv().getTypeUtils().asElement(returnType);
        if (returnTypeAsElement != null && returnTypeAsElement.getKind() == ElementKind.TYPE_PARAMETER) {
            this.propertyGenericElement = (TypeParameterElement) returnTypeAsElement;
            this.propertyType = new Property(this.propertyGenericElement.asType());
            this.propertyTypeElement = null;
        } else if (returnTypeAsElement instanceof TypeElement) {
            this.propertyTypeElement = (TypeElement) returnTypeAsElement;
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
        this.addInnerClassParent();
        this.addInnerClassGet();
        this.addInnerClassGetWithRoot();
        this.addInnerClassSet();
        this.addInnerClassSetWithRoot();
        this.addInnerClassGetContainedTypeIfNeeded();
    }

    private void addOuterClassGet() {
        GMethod fieldGet = this.bindingClass.getMethod(this.propertyName + "()");
        if (this.propertyGenericElement != null) {
            fieldGet.returnType(this.propertyType.getInnerClass(this.propertyName));
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

    private void addOuterClassBindingField() {
        GField f = this.bindingClass.getField(this.propertyName).type(this.propertyType.getBindingClassFieldDeclaration(this.propertyName));
        if (this.propertyType.isRawType()) {
            f.addAnnotation("@SuppressWarnings(\"unchecked\")");
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

    private void addInnerClassParent() {
        GMethod getParent = this.innerClass.getMethod("getParentBinding").returnType("Binding<?>").addAnnotation("@Override");
        getParent.body.line("return {}.this;", this.bindingClass.getSimpleClassNameWithoutGeneric());
    }

    private void addInnerClassGet() {
        GMethod get = this.innerClass.getMethod("get");
        get.returnType(this.propertyType.getSetType()).addAnnotation("@Override");
        get.body.line("return {}{}.this.get().{}();",//
            this.propertyType.getCastForReturnIfNeeded(),
            this.bindingClass.getSimpleClassNameWithoutGeneric(),
            this.methodName);
        if (this.propertyType.isFixingRawType) {
            get.addAnnotation("@SuppressWarnings(\"unchecked\")");
        }
    }

    private void addInnerClassGetWithRoot() {
        GMethod getWithRoot = this.innerClass.getMethod("getWithRoot");
        getWithRoot.argument("R", "root").returnType(this.propertyType.getSetType()).addAnnotation("@Override");
        getWithRoot.body.line("return {}{}.this.getWithRoot(root).{}();",//
            this.propertyType.getCastForReturnIfNeeded(),
            this.bindingClass.getSimpleClassNameWithoutGeneric(),
            this.methodName);
        if (this.propertyType.isFixingRawType) {
            getWithRoot.addAnnotation("@SuppressWarnings(\"unchecked\")");
        }
    }

    private void addInnerClassSet() {
        GMethod set = this.innerClass.getMethod("set({} {})", this.propertyType.getSetType(), this.propertyName); // .addAnnotation("@Override");
        if (!this.hasSetter()) {
            set.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
            return;
        }
        set.body.line("{}.this.get().{}({});",//
            this.bindingClass.getSimpleClassNameWithoutGeneric(),
            this.getSetterName(),
            this.propertyName);
    }

    private void addInnerClassSetWithRoot() {
        GMethod setWithRoot = this.innerClass.getMethod("setWithRoot(R root, {} {})", this.propertyType.getSetType(), this.propertyName); // .addAnnotation("@Override");
        if (!this.hasSetter()) {
            setWithRoot.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
            return;
        }
        setWithRoot.body.line("{}.this.getWithRoot(root).{}({});",//
            this.bindingClass.getSimpleClassNameWithoutGeneric(),
            this.getSetterName(),
            this.propertyName);
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
