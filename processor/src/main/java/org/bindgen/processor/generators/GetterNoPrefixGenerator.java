package org.bindgen.processor.generators;

import java.util.Collection;

import javax.lang.model.element.ExecutableElement;

import joist.sourcegen.GClass;

/**
 * Generates bindings for no-arg methods that return something (non void) 
 * and do not start with a specific getter prefix
 *
 */
public class GetterNoPrefixGenerator extends GetterMethodGenerator {

	public GetterNoPrefixGenerator(GClass outerClass, ExecutableElement method, Collection<String> namesTaken) throws WrongGeneratorException {
		super(outerClass, method, namesTaken);
	}

	@Override
	protected boolean checkViability() {
		return AccessorPrefix.NONE == this.prefix && !this.hasSetterMethod() && this.methodNotVoidNoParamsNoThrows();
	}

	public static class Factory extends ExecutableElementGeneratorFactory {
		@Override
		public GetterNoPrefixGenerator newGenerator(GClass outerClass, ExecutableElement method, Collection<String> namesTaken) throws WrongGeneratorException {
			return new GetterNoPrefixGenerator(outerClass, method, namesTaken);
		}
	}

}
