package org.bindgen.processor.generators;

import javax.lang.model.element.ExecutableElement;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;

/**
 * Generates bindings for get/set method pairs
 * @author mihai
 *
 */
public class AccessorMethodGenerator extends MethodBindingGenerator {

	public AccessorMethodGenerator(GClass outerClass, ExecutableElement method) throws WrongGeneratorException {
		super(outerClass, method);
	}

	@Override
	protected void checkViability() throws WrongGeneratorException {
		if (!this.hasSetterMethod() || this.methodReturnsVoid() || this.methodHasParameters() || this.methodThrowsExceptions()) {
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
		this.addInnerClassSet();
		this.addInnerClassSetWithRoot();
		this.addInnerClassGetContainedTypeIfNeeded();
		this.addInnerClassSerialVersionUID();
	}

	private void addInnerClassSet() {
		GMethod set = this.innerClass.getMethod("set({} {})", this.property.getSetType(), this.property.getName());
		set.addAnnotation("@Override");
		set.body.line("{}.this.get().{}({});",//
			this.outerClass.getSimpleClassNameWithoutGeneric(),
			this.prefix.setterName(this.methodName),
			this.property.getName());
	}

	private void addInnerClassSetWithRoot() {
		GMethod setWithRoot = this.innerClass.getMethod("setWithRoot(R root, {} {})", this.property.getSetType(), this.property.getName());
		setWithRoot.addAnnotation("@Override");
		setWithRoot.body.line("{}.this.getWithRoot(root).{}({});",//
			this.outerClass.getSimpleClassNameWithoutGeneric(),
			this.prefix.setterName(this.methodName),
			this.property.getName());
	}

	public static class Factory extends ExecutableElementGeneratorFactory {
		@Override
		public AccessorMethodGenerator newGenerator(GClass outerClass, ExecutableElement method) throws WrongGeneratorException {
			return new AccessorMethodGenerator(outerClass, method);
		}
	}
}
