package org.bindgen.processor.generators;

import javax.lang.model.element.ExecutableElement;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;

/**
 * Generates bindings for getter methods 
 * @author mihai
 *
 */
public class GetterMethodGenerator extends AbstractMethodBindingGenerator {

	public GetterMethodGenerator(GClass outerClass, ExecutableElement method) throws WrongGeneratorException {
		super(outerClass, method);
	}

	@Override
	protected void checkViability() throws WrongGeneratorException {
		if (AccessorPrefix.NONE == this.prefix
			|| this.hasSetterMethod()
			|| this.methodReturnsVoid()
			|| this.methodHasParameters()
			|| this.methodThrowsExceptions()) {
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
		set.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
		return;
	}

	private void addInnerClassSetWithRoot() {
		GMethod setWithRoot = this.innerClass.getMethod("setWithRoot(R root, {} {})", this.property.getSetType(), this.property.getName());
		setWithRoot.addAnnotation("@Override");
		setWithRoot.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
	}

	public static class Factory extends ExecutableElementGeneratorFactory {
		@Override
		public GetterMethodGenerator newGenerator(GClass outerClass, ExecutableElement method) throws WrongGeneratorException {
			return new GetterMethodGenerator(outerClass, method);
		}
	}

}
