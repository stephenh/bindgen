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
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

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
 * The second class is the <ocde>XxxBinding</code> which extends its
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
		this.generateProperties();
		this.addGetChildBindings();

		this.initializeRootBindingClass();
		this.addConstructors();
		this.addGetWithRoot();

		this.addGeneratedTimestamp();
		this.addSerialVersionUID();
		this.saveCode(this.pathBindingClass);
		this.saveCode(this.rootBindingClass);
	}

	private void initializePathBindingClass() {
		this.pathBindingClass = new GClass(this.name.getBindingPathClassDeclaration());
		this.pathBindingClass.baseClassName(this.name.getBindingPathClassSuperClass());
		this.pathBindingClass.setAbstract();
	}

	private void initializeRootBindingClass() {
		this.rootBindingClass = new GClass(this.name.getBindingRootClassDeclaration());
		this.rootBindingClass.baseClassName(this.name.getBindingRootClassSuperClass());
	}

	private void addGetWithRoot() {
		GMethod getWithRoot = this.rootBindingClass.getMethod("getWithRoot").argument(this.name.get(), "root").returnType(this.name.get());
		getWithRoot.body.line("return root;");
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
		getType.body.line("return {}.class;", this.element.getSimpleName());
	}

	private void generateProperties() {
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
			getMessager().printMessage(Kind.ERROR, io.getMessage());
		}
	}

	private List<PropertyGenerator> getPropertyGenerators() {
		List<PropertyGenerator> generators = new ArrayList<PropertyGenerator>();

		Set<String> namesTaken = new HashSet<String>();

		// TODO all ths stuff below need implementing somehow
		// Do methods first so that if a field/method overlap, the getter/setter take precedence
		List<PropertyGenerator.GeneratorFactory> factories = new ArrayList<PropertyGenerator.GeneratorFactory>();

		// these bindings will always keep their name
		factories.add(new NoArgMethodGenerator.Factory());

		// in case of name clash these bindings will not drop their prefix
		factories.add(new GetterMethodGenerator.Factory());

		// in case of name clash, these bindings will also keep their prefix
		factories.add(new AccessorMethodGenerator.Factory());

		// XXX I don't know about these ones
		factories.add(new MethodCallableGenerator.Factory());

		// in case of name clash with an accessor, these bindings will not be generated
		// in case of name clash with anything else, the suffix "Field" will be appended to the binding name
		factories.add(new FieldPropertyGenerator.Factory());

		// get accessible elements
		List<? extends Element> elements = getElementUtils().getAllMembers(this.element);
		List<Element> accesibleElements = new ArrayList<Element>(elements.size());
		for (Element enclosed : elements) {
			if (Util.isAccessibleIfGenerated(this.element, enclosed)) {
				accesibleElements.add(enclosed);
			}
		}

		for (PropertyGenerator.GeneratorFactory f : factories) {
			Iterator<? extends Element> it = accesibleElements.iterator();
			while (it.hasNext()) {
				Element enclosed = it.next();
				try {
					PropertyGenerator pg = f.newGenerator(this.pathBindingClass, enclosed, namesTaken);
					if (namesTaken.contains(pg.getPropertyName())) {
						continue;
					} else {
						namesTaken.add(pg.getPropertyName());
					}
					it.remove(); // element is handled, other PropertyGenerators should not even bother  
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
}
