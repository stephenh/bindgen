package org.bindgen.processor.generators;

import java.util.List;

import javax.lang.model.element.ExecutableElement;

import joist.sourcegen.GClass;

/**
 * Generates bindings for no-arg methods that return something (non void) 
 * @author mihai
 *
 */
public class NoArgMethodGenerator extends GetterMethodGenerator {

	public NoArgMethodGenerator(GClass outerClass, ExecutableElement method, List<String> namesTaken) throws WrongGeneratorException {
		super(outerClass, method, namesTaken);
	}

	@Override
	protected boolean checkViability() {
		return AccessorPrefix.NONE == this.prefix && !this.hasSetterMethod() && this.methodNotVoidNoParamsNoThrows();
	}

	public static class Factory extends ExecutableElementGeneratorFactory {
		@Override
		public NoArgMethodGenerator newGenerator(GClass outerClass, ExecutableElement method, List<String> namesTaken) throws WrongGeneratorException {
			return new NoArgMethodGenerator(outerClass, method, namesTaken);
		}
	}

}
