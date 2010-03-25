package org.bindgen.processor.generators;

import java.util.Collection;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import joist.sourcegen.GClass;

/** Common methods for property (e.g. method/field/callable) generators. */
public interface PropertyGenerator {

	/** @return the property name, e.g. the method/field name */
	String getPropertyName();

	/** @return the property name, e.g. field type or method get/set type */
	TypeElement getPropertyTypeElement();

	/** @return whether this binding has any sub-bindings that need to be registered */
	boolean hasSubBindings();

	/** Generates the property binding. */
	void generate();

	public static interface GeneratorFactory {
		PropertyGenerator newGenerator(GClass outerClass, TypeElement outerElement, Element element, Collection<String> namesTaken) throws WrongGeneratorException;
	}
}
