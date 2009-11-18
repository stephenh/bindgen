package org.bindgen.processor.util;

import static org.bindgen.processor.CurrentEnv.*;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import joist.sourcegen.Access;
import joist.util.Inflector;

public class Util {

	private static final Pattern outerClassName = Pattern.compile("\\.([A-Z]\\w+)\\.");
	private static final String[] javaKeywords = "abstract,continue,for,new,switch,assert,default,goto,package,synchronized,boolean,do,if,private,this,break,double,implements,protected,throw,byte,else,import,public,throws,case,enum,instanceof,return,transient,catch,extends,int,short,try,char,final,interface,static,void,class,finally,long,strictfp,volatile,const,float,native,super,while,null"
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
		if (type.getKind() == TypeKind.ARRAY) {
			type = ((ArrayType) type).getComponentType();
		}
		if (type.getKind().isPrimitive()) {
			return getTypeUtils().boxedClass((PrimitiveType) type).asType();
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

	/**
	 * Gets the {@link Access} level of the element
	 * @param element
	 * @return
	 */
	public static Access getAccess(Element element) {
		final Set<Modifier> modifiers = element.getModifiers();
		if (modifiers.contains(Modifier.PUBLIC)) {
			return Access.PUBLIC;
		} else if (modifiers.contains(Modifier.PROTECTED)) {
			return Access.PROTECTED;
		} else if (modifiers.contains(Modifier.PRIVATE)) {
			return Access.PRIVATE;
		} else {
			return Access.PACKAGE;
		}
	}
}
