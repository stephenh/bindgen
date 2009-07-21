package org.exigencecorp.bindgen.processor.generators;

import javax.lang.model.element.TypeElement;

public interface PropertyGenerator {

    String getPropertyName();

    TypeElement getPropertyTypeElement();

    boolean shouldGenerate();

    boolean isCallable();

    void generate();

}
