package org.bindgen.processor.generators;

import static org.bindgen.processor.CurrentEnv.*;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Generated;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;
import joist.util.Copy;

import org.bindgen.Binding;
import org.bindgen.processor.GenerationQueue;
import org.bindgen.processor.Processor;
import org.bindgen.processor.util.BoundClass;
import org.bindgen.processor.util.Util;

/** Generates a <code>XxxBinding</code> class for a given {@link TypeElement}.
 *
 * Two classes are generated: one class is an abstract <code>XxxBindingPath</code>
 * which has a generic parameter <code>R</code> to present one part in
 * a binding evaluation path rooted at type a type <code>R</code>.
 *
 * The second class is the <code>XxxBinding</code> which extends its
 * <code>XxxBindingPath</code> but provides the type parameter <code>R</code>
 * as <code>Xxx</code>, meaning that <code>XxxBinding</code> can be
 * used as the starting point for binding paths rooted at a <code>Xxx</code>.
 */
public class BindingClassGenerator {

	private final GenerationQueue queue;
	private final TypeElement element;
	private final BoundClass name;
	private final List<String> foundSubBindings = new ArrayList<String>();
	private final Set<Element> sourceElements = new HashSet<Element>();
	private GClass pathBindingClass;
	private GClass rootBindingClass;

	public BindingClassGenerator(GenerationQueue queue, TypeElement element) {
		this.queue = queue;
		this.element = element;
		this.name = new BoundClass(element);
	}

	public void generate() {
		this.initializePathBindingClass();
		this.addGetName();
		this.addGetType();
		this.addProperties();
		this.addGetChildBindings();

		this.initializeRootBindingClass();
		this.addConstructors();
		this.addGetWithRoot();
		this.addGetSafelyWithRoot();

		this.addGeneratedTimestamp();
		this.addSerialVersionUID();
		this.saveCode(this.pathBindingClass);
		this.saveCode(this.rootBindingClass);
	}

	private void initializePathBindingClass() {
		this.pathBindingClass = new GClass(this.name.getBindingPathClassDeclaration());
		this.pathBindingClass.baseClassName(this.name.getBindingPathClassSuperClass());
		this.pathBindingClass.setAbstract();
		this.pathBindingClass.addAnnotation("@SuppressWarnings(\"all\")");
	}

	private void initializeRootBindingClass() {
		this.rootBindingClass = new GClass(this.name.getBindingRootClassDeclaration());
		this.rootBindingClass.baseClassName(this.name.getBindingRootClassSuperClass());
		this.rootBindingClass.addAnnotation("@SuppressWarnings(\"all\")");
	}

	private void addGetWithRoot() {
		GMethod getWithRoot = this.rootBindingClass.getMethod("getWithRoot").argument(this.name.get(), "root").returnType(this.name.get());
		getWithRoot.body.line("return root;");
	}

	private void addGetSafelyWithRoot() {
		GMethod getSafelyWithRoot = this.rootBindingClass.getMethod("getSafelyWithRoot").argument(this.name.get(), "root").returnType(this.name.get());
		getSafelyWithRoot.body.line("return root;");
	}

	private void addGeneratedTimestamp() {
		if (getConfig().skipGeneratedTimestamps()) {
			return;
		}
		String value = Processor.class.getName();
		String date = new SimpleDateFormat("dd MMM yyyy hh:mm").format(new Date());
		this.pathBindingClass.addImports(Generated.class);
		this.pathBindingClass.addAnnotation("@Generated(value = \"" + value + "\", date = \"" + date + "\")");
		this.rootBindingClass.addImports(Generated.class);
		this.rootBindingClass.addAnnotation("@Generated(value = \"" + value + "\", date = \"" + date + "\")");
	}

	private void addConstructors() {
		this.rootBindingClass.getConstructor();
		this.rootBindingClass.getConstructor(this.name.get() + " value").body.line("this.set(value);");
	}

	private void addGetName() {
		GMethod getName = this.pathBindingClass.getMethod("getName").returnType(String.class).addAnnotation("@Override");
		getName.body.line("return \"\";");
	}

	private void addGetType() {
		GMethod getType = this.pathBindingClass.getMethod("getType").returnType("Class<?>").addAnnotation("@Override");
		getType.body.line("return {}.class;", this.element.toString());
	}

	private void addProperties() {
		for (PropertyGenerator pg : this.getPropertyGenerators()) {
			pg.generate();
			this.enqueuePropertyTypeIfNeeded(pg);
			this.addToSubBindingsIfNeeded(pg);
		}
	}

	private void enqueuePropertyTypeIfNeeded(PropertyGenerator pg) {
		if (pg.getPropertyTypeElement() != null) {
			if (getConfig().shouldGenerateBindingFor(pg.getPropertyTypeElement())) {
				this.queue.enqueueIfNew(pg.getPropertyTypeElement());
			}
		}
	}

	private void addToSubBindingsIfNeeded(PropertyGenerator pg) {
		if (pg.hasSubBindings()) {
			this.foundSubBindings.add(pg.getPropertyName());
		}
	}

	private void addGetChildBindings() {
		this.pathBindingClass.addImports(Binding.class, List.class);
		GMethod children = this.pathBindingClass.getMethod("getChildBindings").returnType("List<Binding<?>>").addAnnotation("@Override");
		children.body.line("List<Binding<?>> bindings = new java.util.ArrayList<Binding<?>>();");
		for (String foundSubBinding : this.foundSubBindings) {
			children.body.line("bindings.add(this.{}());", foundSubBinding);
		}
		children.body.line("return bindings;");
	}

	private void saveCode(GClass gc) {
		try {
			JavaFileObject jfo = getFiler()
				.createSourceFile(gc.getFullClassNameWithoutGeneric(), Copy.array(Element.class, Copy.list(this.sourceElements)));
			Writer w = jfo.openWriter();
			w.write(gc.toCode());
			w.close();
			this.queue.log("Saved " + gc.getFullClassNameWithoutGeneric());
		} catch (IOException io) {
			getMessager().printMessage(Kind.ERROR, io.getMessage(), this.element);
		}
	}

	private List<PropertyGenerator> getPropertyGenerators() {
		// factory ordering specifies binding precedence rules
		List<PropertyGenerator.GeneratorFactory> factories = new ArrayList<PropertyGenerator.GeneratorFactory>();
		// these bindings will not mangle their property names
		factories.add(new MethodPropertyGenerator.Factory(AccessorPrefix.NONE));
		factories.add(new MethodCallableGenerator.Factory());
		// these bindings will try to drop their prefix and use a shorter name (e.g. getFoo -> foo)
		factories.add(new MethodPropertyGenerator.Factory(AccessorPrefix.GET));
		factories.add(new MethodPropertyGenerator.Factory(AccessorPrefix.HAS));
		factories.add(new MethodPropertyGenerator.Factory(AccessorPrefix.IS));
		// the field binding will use its name or append Field if it was already taken by get/has/is
		factories.add(new FieldPropertyGenerator.Factory());

		Set<String> namesTaken = new HashSet<String>();
		namesTaken.add("getName");
		namesTaken.add("getPath");
		namesTaken.add("getType");
		namesTaken.add("getParentBinding");
		namesTaken.add("getChildBindings");

		List<Element> elements = this.getAccessibleElements();
		List<PropertyGenerator> generators = new ArrayList<PropertyGenerator>();

		for (PropertyGenerator.GeneratorFactory f : factories) {
			for (Iterator<Element> i = elements.iterator(); i.hasNext();) {
				Element enclosed = i.next();
				try {
					PropertyGenerator pg = f.newGenerator(this.pathBindingClass, this.element, enclosed, namesTaken);
					if (namesTaken.contains(pg.getPropertyName())) {
						continue;
					} else {
						namesTaken.add(pg.getPropertyName());
					}
					i.remove(); // element is handled, skip any further generators
					generators.add(pg);
					this.sourceElements.add(enclosed);
				} catch (WrongGeneratorException e) {
					// try next
				}
			}
		}
		return generators;
	}

	private void addSerialVersionUID() {
		this.rootBindingClass.getField("serialVersionUID").type("long").setStatic().setFinal().initialValue("1L");
		this.pathBindingClass.getField("serialVersionUID").type("long").setStatic().setFinal().initialValue("1L");
	}

	private List<Element> getAccessibleElements() {
		List<Element> elements = new ArrayList<Element>();
		for (Element enclosed : getElementUtils().getAllMembers(this.element)) {
			if (Util.isAccessibleIfGenerated(this.element, enclosed)) {
				elements.add(enclosed);
			}
		}
		return elements;
	}
}
