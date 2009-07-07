package org.exigencecorp.bindgen.processor;

import static org.exigencecorp.bindgen.processor.CurrentEnv.getTypeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

import joist.util.Inflector;

public class Util {

    private static final Pattern outerClassName = Pattern.compile("\\.([A-Z]\\w+)\\.");

    // Watch for package.Foo.Inner -> package.foo.Inner
    public static String lowerCaseOuterClassNames(String className) {
        Matcher m = outerClassName.matcher(className);
        while (m.find()) {
            className = m.replaceFirst("." + Inflector.uncapitalize(m.group(1)) + ".");
            m = outerClassName.matcher(className);
        }
        return className;
    }

    public static TypeMirror boxIfNeeded(TypeMirror type) {
        if (type instanceof PrimitiveType) {
            // double check--Eclipse worked fine but javac is letting non-primitive types in here
            if (type.toString().indexOf('.') == -1) {
                try {
                    return getTypeUtils().boxedClass((PrimitiveType) type).asType();
                } catch (NullPointerException npe) {
                    return type; // it is probably a type parameter, e.g. T
                }
            }
        }
        return type;
    }

}
