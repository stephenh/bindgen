package org.bindgen.processor.generators;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;

import joist.sourcegen.GClass;
import joist.sourcegen.GField;
import joist.sourcegen.GMethod;
import joist.util.Inflector;

import org.bindgen.ContainerBinding;
import org.bindgen.processor.util.BoundProperty;
import org.bindgen.processor.util.Util;

/**
 * Generates bindings for methods (class exists for implementation reuse only) 
 * @author mihai
 *
 */
public abstract class MethodBindingGenerator implements PropertyGenerator {
	private static final String[] illegalPropertyNames = { "hashCode", "toString", "clone" };

	public static enum AccessorPrefix {
		NONE("", "") {
			public String setterName(String getterMethodName) {
				return getterMethodName;
			}

			public String propertyName(String getterMethodName) {
				if (Util.isJavaKeyword(getterMethodName) || "get".equals(getterMethodName)) {
					return null;
				}
				for (String illegalProp : illegalPropertyNames) {
					if (illegalProp.equals(getterMethodName)) {
						return null;
					}
				}

				return getterMethodName;
			}
		},

		GET("get", "set"), IS("is", "set"), HAS("has", "set");

		public final String getterPrefix, setterPrefix;

		private AccessorPrefix(String getterPrefix, String setterPrefix) {
			this.setterPrefix = setterPrefix;
			this.getterPrefix = getterPrefix;
		}

		public String setterName(String getterMethodName) {
			return this.setterPrefix + getterMethodName.substring(this.getterPrefix.length());
		}

		public String propertyName(String getterMethodName) {
			String propertyName = Inflector.uncapitalize(getterMethodName.substring(this.getterPrefix.length()));
			if (Util.isJavaKeyword(propertyName) || "get".equals(propertyName)) {
				propertyName = getterMethodName;
			}
			for (String illegalProp : illegalPropertyNames) {
				if (illegalProp.equals(propertyName)) {
					propertyName = getterMethodName;
					break;
				}
			}

			return propertyName;
		}

		public static AccessorPrefix guessPrefix(String getterMethodName) {
			AccessorPrefix[] values = values();
			for (int i = 1; i < values.length; i++) {
				AccessorPrefix possiblePrefix = values[i];
				String possible = possiblePrefix.getterPrefix;
				if (getterMethodName.startsWith(possible)
					&& getterMethodName.length() > possible.length() + 1
					&& getterMethodName.substring(possible.length(), possible.length() + 1).matches("[A-Z]")) {
					return possiblePrefix;
				}
			}
			return NONE;
		}
	}

	protected final AccessorPrefix prefix;
	protected final GClass outerClass;
	protected final ExecutableElement method;
	protected final String methodName;
	protected final BoundProperty property;
	protected GClass innerClass;

	public MethodBindingGenerator(GClass outerClass, ExecutableElement method) throws WrongGeneratorException {
		this.outerClass = outerClass;
		this.method = method;
		this.methodName = method.getSimpleName().toString();
		this.prefix = AccessorPrefix.guessPrefix(this.methodName);
		this.property = new BoundProperty(this.method, this.method.getReturnType(), this.prefix.propertyName(this.methodName));
		this.checkViability();
		if (this.property.shouldSkip()) {
			throw new WrongGeneratorException();
		}
	}

	protected abstract void checkViability() throws WrongGeneratorException;

	protected boolean hasSetterMethod() {
		String setterName = this.prefix.setterName(this.methodName);
		TypeElement parent = (TypeElement) this.method.getEnclosingElement();
		for (Element enclosed : parent.getEnclosedElements()) {
			String memberName = enclosed.getSimpleName().toString();
			if (memberName.equals(setterName) && Util.isAccessibleIfGenerated(parent, parent, enclosed)) {
				ExecutableElement e = (ExecutableElement) enclosed;
				return e.getParameters().size() == 1 // single parameter 
					&& e.getThrownTypes().isEmpty() // no throws
					&& e.getParameters().get(0).asType() == this.method.getReturnType(); // types match
			}
		}
		return false;
	}

	protected boolean methodThrowsExceptions() {
		return !((ExecutableType) this.method.asType()).getThrownTypes().isEmpty();
	}

	protected boolean methodHasParameters() {
		return !((ExecutableType) this.method.asType()).getParameterTypes().isEmpty();
	}

	protected boolean methodReturnsVoid() {
		return ((ExecutableType) this.method.asType()).getReturnType().getKind() == TypeKind.VOID;
	}

	protected void addOuterClassGet() {
		GMethod fieldGet = this.outerClass.getMethod(this.property.getName() + "()");
		fieldGet.setAccess(Util.getAccess(this.method));
		fieldGet.returnType(this.property.getBindingClassFieldDeclaration());
		fieldGet.body.line("if (this.{} == null) {", this.property.getName());
		fieldGet.body.line("    this.{} = new {}();", this.property.getName(), this.property.getBindingRootClassInstantiation());
		fieldGet.body.line("}");
		fieldGet.body.line("return this.{};", this.property.getName());
		if (this.property.doesOuterGetNeedSuppressWarnings()) {
			fieldGet.addAnnotation("@SuppressWarnings(\"unchecked\")");
		}
	}

	protected void addOuterClassBindingField() {
		GField f = this.outerClass.getField(this.property.getName()).type(this.property.getBindingClassFieldDeclaration());
		if (this.property.isRawType()) {
			f.addAnnotation("@SuppressWarnings(\"unchecked\")");
		}
	}

	protected void addInnerClass() {
		this.innerClass = this.outerClass.getInnerClass(this.property.getInnerClassDeclaration()).notStatic();
		this.innerClass.setAccess(Util.getAccess(this.method));
		this.innerClass.baseClassName(this.property.getInnerClassSuperClass());
		if (this.property.doesInnerClassNeedSuppressWarnings()) {
			this.innerClass.addAnnotation("@SuppressWarnings(\"unchecked\")");
		}
		if (this.property.isForGenericTypeParameter() || this.property.isArray()) {
			this.innerClass.getMethod("getType").returnType("Class<?>").body.line("return null;");
		} else if (!this.property.shouldGenerateBindingClassForType()) {
			// since no binding class will be generated for the return type of this method we may not inherit getType() in MyBinding class (if, for example, MyBinding extends GenericObjectBindingPath) and so we have to implement it ouselves
			this.innerClass.getMethod("getType").returnType("Class<?>").body.line("return {}.class;", this.property.getReturnableType());
		}
	}

	protected void addInnerClassGetName() {
		GMethod getName = this.innerClass.getMethod("getName").returnType(String.class).addAnnotation("@Override");
		getName.body.line("return \"{}\";", this.property.getName());
	}

	protected void addInnerClassParent() {
		GMethod getParent = this.innerClass.getMethod("getParentBinding").returnType("Binding<?>").addAnnotation("@Override");
		getParent.body.line("return {}.this;", this.outerClass.getSimpleClassNameWithoutGeneric());
	}

	protected void addInnerClassGet() {
		GMethod get = this.innerClass.getMethod("get");
		get.returnType(this.property.getSetType()).addAnnotation("@Override");
		get.body.line("return {}{}.this.get().{}();",//
			this.property.getCastForReturnIfNeeded(),
			this.outerClass.getSimpleClassNameWithoutGeneric(),
			this.methodName);
		if (this.property.doesInnerGetNeedSuppressWarnings()) {
			get.addAnnotation("@SuppressWarnings(\"unchecked\")");
		}
	}

	protected void addInnerClassGetWithRoot() {
		GMethod getWithRoot = this.innerClass.getMethod("getWithRoot");
		getWithRoot.argument("R", "root").returnType(this.property.getSetType()).addAnnotation("@Override");
		getWithRoot.body.line("return {}{}.this.getWithRoot(root).{}();",//
			this.property.getCastForReturnIfNeeded(),
			this.outerClass.getSimpleClassNameWithoutGeneric(),
			this.methodName);
		if (this.property.doesInnerGetNeedSuppressWarnings()) {
			getWithRoot.addAnnotation("@SuppressWarnings(\"unchecked\")");
		}
	}

	protected void addInnerClassGetContainedTypeIfNeeded() {
		if (this.property.isForListOrSet() && !this.property.matchesTypeParameterOfParent()) {
			this.innerClass.implementsInterface(ContainerBinding.class);
			GMethod getContainedType = this.innerClass.getMethod("getContainedType").returnType("Class<?>").addAnnotation("@Override");
			getContainedType.body.line("return {};", this.property.getContainedType());
		}
	}

	protected void addInnerClassSerialVersionUID() {
		this.innerClass.getField("serialVersionUID").type("long").setStatic().setFinal().initialValue("1L");
	}

	@Override
	public String getPropertyName() {
		return this.property.getName();
	}

	@Override
	public TypeElement getPropertyTypeElement() {
		return this.property.getElement();
	}

	@Override
	public boolean hasSubBindings() {
		return true;
	}

	public abstract static class ExecutableElementGeneratorFactory implements GeneratorFactory {
		@Override
		public MethodBindingGenerator newGenerator(GClass outerClass, Element possibleMethod) throws WrongGeneratorException {
			if (possibleMethod.getKind() != ElementKind.METHOD) {
				throw new WrongGeneratorException();
			}
			return this.newGenerator(outerClass, (ExecutableElement) possibleMethod);
		}

		public abstract MethodBindingGenerator newGenerator(GClass outerClass, ExecutableElement method) throws WrongGeneratorException;

	}
}
