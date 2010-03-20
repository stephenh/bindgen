package org.bindgen.processor.generators;

import java.util.Collection;

import javax.lang.model.element.ExecutableElement;

import joist.sourcegen.GClass;

public class AccessorNoPrefixMethodGenerator extends AccessorMethodGenerator {

	public AccessorNoPrefixMethodGenerator(GClass outerClass, ExecutableElement method, Collection<String> namesTaken) throws WrongGeneratorException {
		super(outerClass, method, namesTaken);
	}

	@Override
	protected boolean checkViability() {
		return AccessorPrefix.NONE == this.prefix && this.hasSetterMethod() && this.methodNotVoidNoParamsNoThrows();
	}

	public static class Factory extends ExecutableElementGeneratorFactory {
		@Override
		public AccessorNoPrefixMethodGenerator newGenerator(GClass outerClass, ExecutableElement method, Collection<String> namesTaken) throws WrongGeneratorException {
			return new AccessorNoPrefixMethodGenerator(outerClass, method, namesTaken);
		}
	}

}
