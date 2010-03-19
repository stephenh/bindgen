package org.bindgen.processor.generators;

import java.util.Collection;

import javax.lang.model.element.ExecutableElement;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;

/**
 * Generates bindings for getter methods that do not have a corresponding setter
 *
 */
public class GetterMethodGenerator extends AbstractMethodBindingGenerator {

	public GetterMethodGenerator(GClass outerClass, ExecutableElement method, Collection<String> namesTaken) throws WrongGeneratorException {
		super(outerClass, method, namesTaken);
	}

	@Override
	protected boolean checkViability() {
		return AccessorPrefix.NONE != this.prefix && !this.hasSetterMethod() && this.methodNotVoidNoParamsNoThrows();
	}

	@Override
	protected void addInnerClassSet() {
		GMethod set = this.innerClass.getMethod("set({} {})", this.property.getSetType(), this.property.getName());
		set.addAnnotation("@Override");
		set.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
		return;
	}

	@Override
	protected void addInnerClassSetWithRoot() {
		GMethod setWithRoot = this.innerClass.getMethod("setWithRoot(R root, {} {})", this.property.getSetType(), this.property.getName());
		setWithRoot.addAnnotation("@Override");
		setWithRoot.body.line("throw new RuntimeException(this.getName() + \" is read only\");");
	}

	public static class Factory extends ExecutableElementGeneratorFactory {
		@Override
		public GetterMethodGenerator newGenerator(GClass outerClass, ExecutableElement method, Collection<String> namesTaken) throws WrongGeneratorException {
			return new GetterMethodGenerator(outerClass, method, namesTaken);
		}
	}

}
