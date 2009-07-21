package org.exigencecorp.bindgen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

@Inherited
@Target(value = { ElementType.TYPE })
public @interface Bindable {

}
