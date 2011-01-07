package org.bindgen.processor.util;

import static org.bindgen.processor.CurrentEnv.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeVariable;

/** A utility class for inspecting/transforming class names to the binding version. */
public class ClassName {

	private final String fullClassNameWithGenerics;

	public ClassName(String fullClassNameWithGenerics) {
		if (fullClassNameWithGenerics.contains(">.")) {
			this.fullClassNameWithGenerics = this.removeRedundantTypeVarsInOuterClasses(fullClassNameWithGenerics);
		} else {
			this.fullClassNameWithGenerics = fullClassNameWithGenerics;
		}
	}

	public String get() {
		return this.fullClassNameWithGenerics;
	}

	public String toString() {
		return this.get();
	}

	/** @return "Type" if the type is "com.app.Type<String, String>" */
	public String getSimpleName() {
		String p = this.getWithoutGenericPart();
		int lastDot = p.lastIndexOf('.');
		if (lastDot == -1) {
			return p;
		} else {
			return p.substring(lastDot + 1);
		}
	}

	/** @return "com.app" if the type is "com.app.Type<String, String>" */
	public String getPackageName() {
		String p = this.getWithoutGenericPart();
		int lastDot = p.lastIndexOf('.');
		if (lastDot == -1) {
			return "";
		} else {
			return p.substring(0, lastDot);
		}
	}

	/** @return ["T", "U"] if the type is "com.app.Type<T extends Foo, U extends Bar>" */
	public List<String> getGenericsWithoutBounds() {
		List<String> args = new ArrayList<String>();
		for (TypeVariable tv : (List<TypeVariable>) this.getDeclaredType().getTypeArguments()) {
			args.add(tv.toString());
		}
		return args;
	}

	/** @return ["T extends Foo", "U extends Bar" if the type is "com.app.Type<T extends Foo, U extends Bar>" */
	public List<String> getGenericsWithBounds() {
		List<String> args = new ArrayList<String>();
		for (TypeVariable tv : (List<TypeVariable>) this.getDeclaredType().getTypeArguments()) {
			String arg = tv.toString();
			if (!Util.isOfTypeObjectOrNone(tv.getUpperBound())) {
				arg += " extends " + tv.getUpperBound().toString();
			}
			args.add(arg);
		}
		return args;
	}

	/** @return "<String, String>" if the type is "com.app.Type<String, String>" or "" if no generics */
	public String getGenericPart() {
		int firstBracket = this.fullClassNameWithGenerics.indexOf("<");
		if (firstBracket != -1) {
			return this.fullClassNameWithGenerics.substring(firstBracket);
		}
		return "";
	}

	/** @return "String, String" if the type is "com.app.Type<String, String>" or "" if no generics */
	public String getGenericPartWithoutBrackets() {
		String type = this.getGenericPart();
		if ("".equals(type)) {
			return type;
		}
		return type.substring(1, type.length() - 1);
	}

	/** @return "com.app.Type" if the type is "com.app.Type<String, String>" */
	public String getWithoutGenericPart() {
		int firstBracket = this.fullClassNameWithGenerics.indexOf("<");
		if (firstBracket != -1) {
			return this.fullClassNameWithGenerics.substring(0, firstBracket);
		}
		return this.fullClassNameWithGenerics;
	}

	private DeclaredType getDeclaredType() {
		TypeElement element = getElementUtils().getTypeElement(this.getWithoutGenericPart());
		return element == null ? null : (DeclaredType) element.asType();
	}

	// For some reason eclipse started returning java.util.Map.Entry<K, V>'s
	// TypeMirror.toString as java.util.Map<K, V>.Entry<K, V>. Map having
	// generics messes up our code generation, so this strips out any generics
	// that are not on the "last" class. So far this hack seems to work.
	private String removeRedundantTypeVarsInOuterClasses(String name) {
		List<Part> generics = new ArrayList<Part>();
		// this is awful, but see the test case for why we're counting open/close brackets
		int lastOpen = 0;
		int openBrackets = 0;
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c == '<') {
				openBrackets++;
				if (openBrackets == 1) {
					lastOpen = i;
				}
			} else if (c == '>') {
				openBrackets--;
				if (openBrackets == 0) {
					generics.add(new Part(lastOpen, i));
				}
			}
		}
		Collections.reverse(generics);
		generics.remove(0); // the last one is okay
		while (!generics.isEmpty()) {
			Part p = generics.remove(0);
			name = name.substring(0, p.begin) + name.substring(p.end + 1);
		}
		return name;
	}

	private static class Part {
		int begin;
		int end;

		private Part(int begin, int end) {
			this.begin = begin;
			this.end = end;
		}
	}

}
