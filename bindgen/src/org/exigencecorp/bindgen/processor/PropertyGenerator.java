package org.exigencecorp.bindgen.processor;

import javax.lang.model.element.TypeElement;

public interface PropertyGenerator {

    String getPropertyName();

    TypeElement getPropertyTypeElement();

    boolean shouldGenerate();

    void generate();

}
