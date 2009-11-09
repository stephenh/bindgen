package org.exigencecorp.bindgen.processor.generators;

import javax.lang.model.element.TypeElement;

/** Common methods for property (e.g. method/field/callable) generators. */
public interface PropertyGenerator {

    /** @return the property name, e.g. the method/field name */
    String getPropertyName();

    /** @return the property name, e.g. field type or method get/set type */
    TypeElement getPropertyTypeElement();

    /** @return whether this property should be generated */
    boolean shouldGenerate();

    /** @return whether this is a callable (e.g. <code>Runnable</code>) binding */
    boolean isCallable();

    /** Generates the property binding. */
    void generate();

}
