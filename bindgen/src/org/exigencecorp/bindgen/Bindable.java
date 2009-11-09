package org.exigencecorp.bindgen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

import org.exigencecorp.bindgen.processor.BindgenAnnotationProcessor;

/** Marks a class to have the {@link BindgenAnnotationProcessor} generate bindings for it. */
@Inherited
@Target(value = { ElementType.TYPE })
public @interface Bindable {

}
