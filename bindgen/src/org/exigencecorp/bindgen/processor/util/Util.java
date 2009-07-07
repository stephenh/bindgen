package org.exigencecorp.bindgen.processor.util;

import static org.exigencecorp.bindgen.processor.CurrentEnv.getTypeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import joist.util.Inflector;

public class Util {

    private static final Pattern outerClassName = Pattern.compile("\\.([A-Z]\\w+)\\.");
    private static final String[] javaKeywords = "abstract,continue,for,new,switch,assert,default,goto,package,synchronized,boolean,do,if,private,this,break,double,implements,protected,throw,byte,else,import,public,throws,case,enum,instanceof,return,transient,catch,extends,int,short,try,char,final,interface,static,void,class,finally,long,strictfp,volatile,const,float,native,super,while"
        .split(",");

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

    public static boolean isOfTypeObjectOrNone(TypeMirror type) {
        return type.getKind() == TypeKind.NONE || type.toString().equals("java.lang.Object");
    }

    public static boolean isJavaKeyword(String name) {
        for (String keyword : javaKeywords) {
            if (keyword.equals(name)) {
                return true;
            }
        }
        return false;
    }

}
