package org.bindgen.processor.generators;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;

/**
 * Generates bindings for get/set method pairs (the bindable class must have both)
 *
 */
public class AccessorMethodGenerator extends AbstractMethodBindingGenerator {

	public AccessorMethodGenerator(GClass outerClass, ExecutableElement method, Collection<String> namesTaken) throws WrongGeneratorException {
		super(outerClass, method, namesTaken);
	}

	@Override
	protected boolean checkViability() {
		return this.hasSetterMethod() && this.methodNotVoidNoParamsNoThrows();
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
		private Set<String> accessorNames = new HashSet<String>();

		@Override
		public AccessorMethodGenerator newGenerator(GClass outerClass, ExecutableElement method, Collection<String> namesTaken) throws WrongGeneratorException {
			AccessorMethodGenerator pg = new AccessorMethodGenerator(outerClass, method, namesTaken);
			this.accessorNames.add(pg.getPropertyName());
			return pg;
		}

		public Collection<String> getAccessorNames() {
			return this.accessorNames;
		}
	}
}
