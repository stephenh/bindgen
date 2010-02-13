package org.bindgen.processor.util;

import static org.bindgen.processor.CurrentEnv.*;

import java.util.Set;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import joist.sourcegen.Access;
import joist.util.Inflector;

public class Util {

	private static final Pattern lowerCase = Pattern.compile("^[a-z]");
	private static final String[] javaKeywords = "abstract,continue,for,new,switch,assert,default,goto,package,synchronized,boolean,do,if,private,this,break,double,implements,protected,throw,byte,else,import,public,throws,case,enum,instanceof,return,transient,catch,extends,int,short,try,char,final,interface,static,void,class,finally,long,strictfp,volatile,const,float,native,super,while,null"
		.split(",");

	// Watch for package.Foo.Inner -> package.foo.Inner
	public static String lowerCaseOuterClassNames(TypeElement bindableClass, String className) {
		if (bindableClass.getEnclosingElement().getKind() == ElementKind.CLASS) {
			String outerClassName = bindableClass.getEnclosingElement().getSimpleName().toString();
			if (lowerCase.matcher(outerClassName).find()) {
				// I'd like to try and generate outerClass$InnerClass, like normal inner classes
				className = className.replace(outerClassName + ".", outerClassName + "_");
			} else {
				className = className.replace(outerClassName, Inflector.uncapitalize(outerClassName));
			}
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

	/**
	 * @param current the type element of the element having bindings generated
	 * @param currentOrSuper the type or super type element of the element having bindings generated
	 * @param enclosed the field or method element
	 */
	public static boolean isAccessibleIfGenerated(TypeElement current, Element enclosed) {
		TypeElement currentOrSuper = (TypeElement) enclosed.getEnclosingElement();
		boolean isStatic = enclosed.getModifiers().contains(Modifier.STATIC);
		boolean isPrivate = enclosed.getModifiers().contains(Modifier.PRIVATE);
		boolean isPublic = enclosed.getModifiers().contains(Modifier.PUBLIC);
		boolean inJava = currentOrSuper.getQualifiedName().toString().startsWith("java.");
		boolean superSamePackage = getElementUtils().getPackageOf(currentOrSuper).equals(getElementUtils().getPackageOf(current));
		return !isStatic && !isPrivate && (isPublic || (superSamePackage && !inJava));
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
