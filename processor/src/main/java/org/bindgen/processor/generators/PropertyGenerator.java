package org.bindgen.processor.generators;

import java.util.Collection;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import joist.sourcegen.GClass;

/** Common methods for property (e.g. method/field/callable) generators. */
public interface PropertyGenerator {

	/** @return the property name, e.g. the method/field name */
	String getPropertyName();

	/** @return the type elements for this signature, to potentially recursively make bindings for */
	List<TypeElement> getPropertyTypeElements();

	/** @return whether this binding has any sub-bindings that need to be registered */
	boolean hasSubBindings();

	/** Generates the property binding. */
	void generate();

	public static interface GeneratorFactory {
		PropertyGenerator newGenerator(GClass outerClass, TypeElement outerElement, Element element, Collection<String> namesTaken) throws WrongGeneratorException;
	}
}
