package org.bindgen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

import org.bindgen.processor.Processor;

/** Marks a class to have the {@link Processor} generate bindings for it. */
@Inherited
@Target(value = { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
public @interface Bindable {

}
