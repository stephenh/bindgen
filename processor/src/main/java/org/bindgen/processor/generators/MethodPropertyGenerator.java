package org.bindgen.processor.generators;

import static org.bindgen.processor.CurrentEnv.*;

import java.util.Collection;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;

import org.bindgen.ContainerBinding;
import org.bindgen.processor.util.BoundProperty;
import org.bindgen.processor.util.Util;

/** Generates bindings for method properties like getFoo/setFoo, foo/foo(), with setters being optional. */
public class MethodPropertyGenerator implements PropertyGenerator {

	private final TypeElement outerElement;
	private final AccessorPrefix prefix;
	private final GClass outerClass;
	private final ExecutableElement method;
	private final String methodName;
	private final BoundProperty property;
	private GClass innerClass;

	public MethodPropertyGenerator(GClass outerClass, TypeElement outerElement, ExecutableElement method, AccessorPrefix prefix, String propertyName)
		throws WrongGeneratorException {
		this.outerElement = outerElement;
		this.outerClass = outerClass;
		this.method = method;
		this.methodName = method.getSimpleName().toString();
		this.prefix = prefix;
		this.property = new BoundProperty(outerElement, this.method, this.method.getReturnType(), propertyName);
		if (!this.methodNotVoidNoParamsNoThrows() || this.property.shouldSkip()) {
			throw new WrongGeneratorException();
		}
	}

	public void generate() {
		this.addOuterClassGet();
		this.addOuterClassBindingField();
		this.addInnerClass();
		this.addInnerClassGetName();
		this.addInnerClassParent();
		this.addInnerClassGet();
		this.addInnerClassGetWithRoot();
		this.addInnerClassGetSafelyWithRoot();
		if (this.hasSetterMethod()) {
			this.addInnerClassSet();
			this.addInnerClassSetWithRoot();
		} else {
			this.addReadOnlyInnerClassSet();
			this.addReadOnlyInnerClassSetWithRoot();
			this.addInnerClassIsReadOnlyOverride();
		}
		this.addInnerClassGetContainedTypeIfNeeded();
		this.addInnerClassSerialVersionUID();
	}

	private boolean hasSetterMethod() {
		String setterName = this.prefix.setterName(this.methodName);

		// Hm...we don't currently go looking into super classes for the setter
		TypeElement parent = (TypeElement) this.method.getEnclosingElement();
		for (ExecutableElement enclosed : ElementFilter.methodsIn(parent.getEnclosedElements())) {
			String methodName = enclosed.getSimpleName().toString();
			if (methodName.equals(setterName) && Util.isAccessibleIfGenerated(parent, enclosed)) {
				// single parameter and no throws
				if (enclosed.getParameters().size() == 1 && enclosed.getThrownTypes().isEmpty()) {
					TypeMirror parameterType = enclosed.getParameters().get(0).asType();
					TypeMirror resolvedParameterType = Util.resolveTypeVarIfPossible(getTypeUtils(), this.outerElement, parameterType).type;
					TypeMirror boxedResolved = Util.boxIfNeeded(resolvedParameterType);
					if (getTypeUtils().isSameType(Util.boxIfNeeded(this.property.getType()), boxedResolved)) {
						return true; // setter parameter type matches getter return type
					}
				}
			}
		}

		return false;
	}

	private boolean methodThrowsExceptions() {
		return !((ExecutableType) this.method.asType()).getThrownTypes().isEmpty();
	}

	private boolean methodHasParameters() {
		return !((ExecutableType) this.method.asType()).getParameterTypes().isEmpty();
	}

	private boolean methodNotVoidNoParamsNoThrows() {
		return !this.methodReturnsVoid() && !this.methodHasParameters() && !this.methodThrowsExceptions();
	}

	private boolean methodReturnsVoid() {
		return ((ExecutableType) this.method.asType()).getReturnType().getKind() == TypeKind.VOID;
	}

	private void addOuterClassGet() {
		GMethod fieldGet = this.outerClass.getMethod(this.property.getName() + "()");
		fieldGet.setAccess(Util.getAccess(this.method));
		fieldGet.returnType(this.property.getBindingClassFieldDeclaration());
		fieldGet.body.line("if (this.{} == null) {", this.property.getName());
		fieldGet.body.line("    this.{} = new {}();", this.property.getName(), this.property.getBindingRootClassInstantiation());
		fieldGet.body.line("}");
		fieldGet.body.line("return this.{};", this.property.getName());
	}

	private void addOuterClassBindingField() {
		this.outerClass.getField(this.property.getName()).type(this.property.getBindingClassFieldDeclaration());
	}

	private void addInnerClass() {
		this.innerClass = this.outerClass.getInnerClass(this.property.getInnerClassDeclaration()).notStatic();
		this.innerClass.setAccess(Util.getAccess(this.method));
		this.innerClass.baseClassName(this.property.getInnerClassSuperClass());
		if (this.property.isForGenericTypeParameter() || this.property.isArray()) {
			this.innerClass.getMethod("getType").returnType("Class<?>").body.line("return null;");
		} else if (!this.property.shouldGenerateBindingClassForType()) {
			// since no binding class will be generated for the return type of this method we may not inherit getType() in MyBinding class (if, for example, MyBinding extends GenericObjectBindingPath) and so we have to implement it ouselves
			this.innerClass.getMethod("getType").returnType("Class<?>").body.line("return {}.class;", this.property.getReturnableType());
		}
	}

	private void addInnerClassGetName() {
		GMethod getName = this.innerClass.getMethod("getName").returnType(String.class).addAnnotation("@Override");
		getName.body.line("return \"{}\";", this.property.getName());
	}

	private void addInnerClassParent() {
		GMethod getParent = this.innerClass.getMethod("getParentBinding").returnType("Binding<?>").addAnnotation("@Override");
		getParent.body.line("return {}.this;", this.outerClass.getSimpleName());
	}

	private void addInnerClassGet() {
		GMethod get = this.innerClass.getMethod("get");
		get.returnType(this.property.getSetType()).addAnnotation("@Override");
		get.body.line("return {}{}.this.get().{}();",//
			this.property.getCastForReturnIfNeeded(),
			this.outerClass.getSimpleName(),
			this.methodName);
	}

	private void addInnerClassGetWithRoot() {
		GMethod getWithRoot = this.innerClass.getMethod("getWithRoot");
		getWithRoot.argument("R", "root").returnType(this.property.getSetType()).addAnnotation("@Override");
		getWithRoot.body.line("return {}{}.this.getWithRoot(root).{}();",//
			this.property.getCastForReturnIfNeeded(),
			this.outerClass.getSimpleName(),
			this.methodName);
	}

	private void addInnerClassGetSafelyWithRoot() {
		GMethod m = this.innerClass.getMethod("getSafelyWithRoot");
		m.argument("R", "root").returnType(this.property.getSetType()).addAnnotation("@Override");
		m.body.line("if ({}.this.getSafelyWithRoot(root) == null) {", this.outerClass.getSimpleName());
		m.body.line("    return null;");
		m.body.line("} else {");
		m.body.line("    return {}{}.this.getWithRoot(root).{}();",//
			this.property.getCastForReturnIfNeeded(),
			this.outerClass.getSimpleName(),
			this.methodName);
		m.body.line("}");
	}

	private void addReadOnlyInnerClassSet() {
		GMethod set = this.innerClass.getMethod("set({} {})", this.property.getSetType(), this.property.getName());
		set.addAnnotation("@Override");
		set.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
		return;
	}

	private void addReadOnlyInnerClassSetWithRoot() {
		GMethod setWithRoot = this.innerClass.getMethod("setWithRoot(R root, {} {})", this.property.getSetType(), this.property.getName());
		setWithRoot.addAnnotation("@Override");
		setWithRoot.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
	}

	private void addInnerClassIsReadOnlyOverride() {
		this.innerClass.getMethod("getBindingIsReadOnly").returnType(boolean.class).body.line("return true;");
	}

	private void addInnerClassSet() {
		GMethod set = this.innerClass.getMethod("set({} {})", this.property.getSetType(), this.property.getName());
		set.addAnnotation("@Override");
		set.body.line("{}.this.get().{}({});",//
			this.outerClass.getSimpleName(),
			this.prefix.setterName(this.methodName),
			this.property.getName());
	}

	private void addInnerClassSetWithRoot() {
		GMethod setWithRoot = this.innerClass.getMethod("setWithRoot(R root, {} {})", this.property.getSetType(), this.property.getName());
		setWithRoot.addAnnotation("@Override");
		setWithRoot.body.line("{}.this.getWithRoot(root).{}({});",//
			this.outerClass.getSimpleName(),
			this.prefix.setterName(this.methodName),
			this.property.getName());
	}

	private void addInnerClassGetContainedTypeIfNeeded() {
		if (this.property.isForListOrSet() && !this.property.matchesTypeParameterOfParent()) {
			this.innerClass.implementsInterface(ContainerBinding.class);
			GMethod getContainedType = this.innerClass.getMethod("getContainedType").returnType("Class<?>").addAnnotation("@Override");
			getContainedType.body.line("return {};", this.property.getContainedType());
		}
	}

	private void addInnerClassSerialVersionUID() {
		this.innerClass.getField("serialVersionUID").type("long").setStatic().setFinal().initialValue("1L");
	}

	@Override
	public String getPropertyName() {
		return this.property.getName();
	}

	@Override
	public List<TypeElement> getPropertyTypeElements() {
		return Util.collectTypeElements(this.property.getType());
	}

	@Override
	public boolean hasSubBindings() {
		return true;
	}

	public static class Factory implements GeneratorFactory {
		private AccessorPrefix prefix;

		public Factory(AccessorPrefix prefix) {
			this.prefix = prefix;
		}

		@Override
		public MethodPropertyGenerator newGenerator(GClass outerClass, TypeElement outerElement, Element possibleMethod, Collection<String> namesTaken) throws WrongGeneratorException {
			if (possibleMethod.getKind() != ElementKind.METHOD) {
				throw new WrongGeneratorException();
			}

			ExecutableElement method = (ExecutableElement) possibleMethod;
			String methodName = method.getSimpleName().toString();
			if (!this.prefix.matches(methodName)) {
				throw new WrongGeneratorException();
			}

			String propertyName = this.prefix.preferredPropertyName(methodName);

			// Sanity check our propertyName
			// 1: If our preferred is already taken or a keyword, fall back
			if (namesTaken.contains(propertyName) || Util.isJavaKeyword(propertyName)) {
				propertyName = methodName;
			}
			// 2: If we'd collide with getType or toString, or the method name is already taken, append Binding
			while (namesTaken.contains(propertyName) || Util.isBindingMethodName(propertyName) || Util.isObjectMethodName(propertyName)) {
				propertyName += "Binding";
			}

			return new MethodPropertyGenerator(outerClass, outerElement, method, this.prefix, propertyName);
		}
	}
}
