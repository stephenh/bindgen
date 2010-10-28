package org.bindgen.processor.util;

import static org.bindgen.processor.CurrentEnv.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import joist.sourcegen.Access;
import joist.util.Inflector;

public class Util {

	private static final Pattern lowerCase = Pattern.compile("^[a-z]");
	private static final String[] javaKeywords = "abstract,continue,for,new,switch,assert,default,goto,package,synchronized,boolean,do,if,private,this,break,double,implements,protected,throw,byte,else,import,public,throws,case,enum,instanceof,return,transient,catch,extends,int,short,try,char,final,interface,static,void,class,finally,long,strictfp,volatile,const,float,native,super,while,null"
		.split(",");
	private static final String[] objectMethodNames = { "hashCode", "toString", "clone", "notify", "notifyAll", "wait", "finalize" };
	private static final String[] bindingMethodNames = { "get", "getPath", "getType", "getParentBinding", "getChildBindings", "getIsSafe", "getSafely" };

	// Watch for package.Foo.Inner -> package.foo.Inner
	public static String lowerCaseOuterClassNames(Element bindableClass, String className) {
		boolean isTypeElement = bindableClass.getKind().isClass() || bindableClass.getKind().isInterface();
		if (isTypeElement) {
			ElementKind enclosingKind = ((TypeElement) bindableClass).getEnclosingElement().getKind();
			if (enclosingKind == ElementKind.CLASS || enclosingKind == ElementKind.INTERFACE || enclosingKind == ElementKind.ENUM) {
				String outerClassName = bindableClass.getEnclosingElement().getSimpleName().toString();
				if (lowerCase.matcher(outerClassName).find()) {
					className = className.replace(outerClassName, "bindgen_" + outerClassName);
				} else {
					className = className.replace(outerClassName, Inflector.uncapitalize(outerClassName));
				}
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

	public static boolean isBadPropertyName(String name) {
		return isJavaKeyword(name) || isObjectMethodName(name) || isBindingMethodName(name);
	}

	public static boolean isJavaKeyword(String name) {
		return contains(javaKeywords, name);
	}

	public static boolean isObjectMethodName(String propertyName) {
		return contains(objectMethodNames, propertyName);
	}

	public static boolean isBindingMethodName(String propertyName) {
		return contains(bindingMethodNames, propertyName);
	}

	/** @return the {@link Access} level of the element */
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

	/** @return super classes + interfaces */
	public static List<TypeMirror> allSuperTypes(TypeElement element) {
		List<TypeMirror> found = new ArrayList<TypeMirror>();

		List<TypeMirror> todo = new ArrayList<TypeMirror>();
		todo.add(element.asType());

		while (todo.size() != 0) {
			TypeMirror currentType = todo.remove(0);
			if (currentType.getKind() != TypeKind.NONE) {
				found.add(currentType);
				Element currentElement = getTypeUtils().asElement(currentType);
				if (currentElement.getKind() == ElementKind.CLASS || currentElement.getKind() == ElementKind.INTERFACE) {
					TypeElement currentTypeElement = (TypeElement) currentElement;
					todo.add(currentTypeElement.getSuperclass());
					todo.addAll(currentTypeElement.getInterfaces());
				}
			}
		}

		// we cheated and added elemenet.asType to found while we
		// were doing the loop--take it back out as it is not a super
		// type of itself
		found.remove(element.asType());

		return found;
	}

	/**
	 * DTO for multiple return values from {@code resolveTypeVarIfPossible}.
	 *
	 * Eclipse will produce NPE-prone TypeMirrors if we use
	 * {@link Types#getDeclaredType(TypeElement, TypeMirror...)} willy
	 * nilly. So we pass around a {@code wasReplaced} variable about
	 * whether we have actually done a type var resolution and, if not,
	 * skip the {@code getDeclaredType} call and return the original,
	 * NPE-avoiding, TypeMirror.
	 */
	public static class ResolveResult {
		public final TypeMirror type;
		public final boolean wasReplaced;

		public ResolveResult(TypeMirror type, boolean wasReplaced) {
			this.type = type;
			this.wasReplaced = wasReplaced;
		}
	}

	public static ResolveResult resolveTypeVarIfPossible(Types types, TypeElement outerElement, TypeMirror type) {
		if (type.getKind() == TypeKind.DECLARED) {
			DeclaredType dt = (DeclaredType) type;
			if (dt.getTypeArguments().size() == 0) {
				return new ResolveResult(dt, false);
			}
			TypeMirror[] args = new TypeMirror[dt.getTypeArguments().size()];
			boolean someReplaced = false;
			int i = 0;
			for (TypeMirror arg : dt.getTypeArguments()) {
				ResolveResult result = resolveTypeVarIfPossible(types, outerElement, arg);
				args[i++] = result.type;
				if (result.wasReplaced) {
					someReplaced = true;
				}
			}
			if (someReplaced) {
				TypeMirror resolved = types.getDeclaredType((TypeElement) dt.asElement(), args);
				return new ResolveResult(resolved, true);
			} else {
				return new ResolveResult(type, false);
			}
		}
		if (type.getKind() != TypeKind.TYPEVAR) {
			return new ResolveResult(type, false);
		}

		// Go searching for the type var
		for (TypeMirror superType : Util.allSuperTypes(outerElement)) {
			boolean found = false;
			int foundAt = 0;

			// Go TypeMirror (bound type vars) -> Element -> TypeMirror (unbound type vars)
			// so that we can compare the possibly-unbound <code>type</code> parameter
			// with the unbound TypeMirror, get the right index, and then jump back to
			// our bound TypeMirror
			TypeMirror superGeneric = getTypeUtils().asElement(superType).asType();
			if (superGeneric.getKind() == TypeKind.DECLARED) {
				for (TypeMirror tm : ((DeclaredType) superGeneric).getTypeArguments()) {
					if (getTypeUtils().isSameType(tm, type)) {
						found = true;
						break;
					}
					foundAt++;
				}
			}

			if (found) {
				if (superType.getKind() == TypeKind.DECLARED) {
					return new ResolveResult(((DeclaredType) superType).getTypeArguments().get(foundAt), true);
				}
			}
		}

		// Didn't find a match
		return new ResolveResult(type, false);
	}

	private static <T> boolean contains(T[] array, T element) {
		for (T a : array) {
			if (a.equals(element)) {
				return true;
			}
		}
		return false;
	}
}
