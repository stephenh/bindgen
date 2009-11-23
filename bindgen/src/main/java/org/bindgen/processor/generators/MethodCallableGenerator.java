package org.bindgen.processor.generators;

import static org.bindgen.processor.CurrentEnv.*;

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

import org.bindgen.NamedBinding;
import org.bindgen.processor.util.Util;

public class MethodCallableGenerator implements PropertyGenerator {

	private final GClass outerClass;
	private final ExecutableElement method;
	private final String methodName;
	private TypeElement blockType;
	private ExecutableElement blockMethod;
	private GClass innerClass;

	public MethodCallableGenerator(GClass outerClass, ExecutableElement method) {
		this.outerClass = outerClass;
		this.method = method;
		this.methodName = this.method.getSimpleName().toString();
	}

	@Override
	public boolean isCallable() {
		return true;
	}

	public boolean shouldGenerate() {
		if (getConfig().skipAttribute(this.method.getEnclosingElement(), this.methodName)) {
			return false;
		}
		for (String classNameToAttempt : getConfig().blockTypesToAttempt()) {
			if (this.blockTypeMatchesMethod(classNameToAttempt)) {
				return true;
			}
		}
		return false;
	}

	public void generate() {
		this.addOuterClassGet();
		this.addOuterClassField();
		this.addInnerClass();
		this.addInnerClassMethod();
		this.addInnerClassGetName();
		this.addInnerClassSerialVersionUID();
	}

	private boolean blockTypeMatchesMethod(String attemptClassName) {
		TypeElement attemptType = getElementUtils().getTypeElement(attemptClassName);
		List<ExecutableElement> methods = ElementFilter.methodsIn(attemptType.getEnclosedElements());
		if (methods.size() != 1) {
			return false; // We only like classes with 1 method
		}
		ExecutableElement methodToMatch = methods.get(0);
		if (this.doBlockReturnTypesMatch(methodToMatch) //
			&& this.doBlockParamsMatch(methodToMatch)
			&& this.doBlockThrowsMatch(methodToMatch)) {
			this.blockType = attemptType;
			this.blockMethod = methodToMatch;
			return true;
		}
		return false;
	}

	private void addOuterClassField() {
		this.outerClass.getField(this.methodName).type(this.blockType.getQualifiedName().toString());
	}

	private void addOuterClassGet() {
		GMethod get = this.outerClass.getMethod(this.methodName).returnType(this.blockType.getQualifiedName().toString());
		get.setAccess(Util.getAccess(this.method));
		get.body.line("if (this.{} == null) {", this.methodName);
		get.body.line("    this.{} = new My{}Binding();", this.methodName, Inflector.capitalize(this.methodName));
		get.body.line("}");
		get.body.line("return this.{};", this.methodName);
	}

	private void addInnerClass() {
		this.innerClass = this.outerClass.getInnerClass("My{}Binding", Inflector.capitalize(this.methodName)).notStatic();
		this.innerClass.setAccess(Util.getAccess(this.method));
		this.innerClass.implementsInterface(this.blockType.getQualifiedName().toString());
		this.innerClass.implementsInterface(NamedBinding.class);
	}

	private void addInnerClassMethod() {
		GMethod run = this.innerClass.getMethod(this.blockMethod.getSimpleName().toString());
		run.returnType(this.blockMethod.getReturnType().toString());
		run.body.line("{}{}.this.get().{}({});",//
			this.getReturnPrefixIfNeeded(),
			this.outerClass.getSimpleClassNameWithoutGeneric(),
			this.methodName,
			this.getArguments());
		this.addMethodParameters(run);
		this.addMethodThrows(run);
	}

	private void addInnerClassGetName() {
		GMethod getName = this.innerClass.getMethod("getName").returnType(String.class);
		getName.body.line("return \"{}\";", this.methodName);
	}

	private void addInnerClassSerialVersionUID() {
		this.innerClass.getField("serialVersionUID").type("long").setStatic().setFinal().initialValue("1L");
	}

	public String getPropertyName() {
		return this.methodName;
	}

	public TypeElement getPropertyTypeElement() {
		return null;
	}

	private boolean doBlockReturnTypesMatch(ExecutableElement methodToMatch) {
		return methodToMatch.getReturnType().equals(this.method.getReturnType());
	}

	private boolean doBlockParamsMatch(ExecutableElement methodToMatch) {
		if (methodToMatch.getParameters().size() != this.getMethodAsType().getParameterTypes().size()) {
			return false;
		}
		boolean allMatch = true;
		for (int i = 0; i < methodToMatch.getParameters().size(); i++) {
			if (!methodToMatch.getParameters().get(i).asType().equals(this.getMethodAsType().getParameterTypes().get(i))) {
				allMatch = false;
			}
		}
		return allMatch;
	}

	private boolean doBlockThrowsMatch(ExecutableElement methodToMatch) {
		for (TypeMirror throwsType : this.method.getThrownTypes()) {
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

	private void addMethodParameters(GMethod run) {
		for (VariableElement foo : this.blockMethod.getParameters()) {
			run.argument(foo.asType().toString(), foo.getSimpleName().toString());
		}
	}

	private void addMethodThrows(GMethod run) {
		for (TypeMirror type : this.method.getThrownTypes()) {
			run.addThrows(type.toString());
		}
	}

	// Figure out whether we need a "return" or not
	private String getReturnPrefixIfNeeded() {
		return this.blockMethod.getReturnType().getKind() == TypeKind.VOID ? "" : "return ";
	}

	private String getArguments() {
		String arguments = "";
		for (VariableElement foo : this.blockMethod.getParameters()) {
			arguments += foo.getSimpleName().toString() + ", ";
		}
		if (arguments.length() > 0) {
			arguments = arguments.substring(0, arguments.length() - 2); // remove last ", "
		}
		return arguments;
	}

	private ExecutableType getMethodAsType() {
		return (ExecutableType) this.method.asType();
	}

}
