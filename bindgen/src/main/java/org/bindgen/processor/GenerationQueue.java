package org.bindgen.processor;

import static org.bindgen.processor.CurrentEnv.getConfig;
import static org.bindgen.processor.CurrentEnv.getFiler;
import static org.bindgen.processor.CurrentEnv.getMessager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

import org.bindgen.processor.generators.BindKeywordGenerator;
import org.bindgen.processor.generators.BindingClassGenerator;
import org.bindgen.processor.util.BoundClass;
import org.bindgen.processor.util.ClassName;

/** Keeps a recursive list of TypeElements to generate bindings for.
 *
 * As each binding is generated, its properties may need their
 * own binding classes generated, so they get put in this queue.
 */
public class GenerationQueue {

	// Both Eclipse and javac will use the same processor instance for each compilation run (e.g. across all of the rounds), so this should be cumulative
	private final Set<String> written = new HashSet<String>();
	// Any TypeElements waiting to have bindings generated
	private final List<TypeElement> queue = new ArrayList<TypeElement>();
	// Whether to log debug messages to System.err
	private final boolean logEnabled;
	// For javac as it does not like the existing bindings
	private final boolean skipExistingBindingCheck;
	// Skip the bindgen.BindKeyword class
	private final boolean skipBindKeyword;

	public GenerationQueue() {
		this.logEnabled = getConfig().logEnabled();
		this.skipExistingBindingCheck = getConfig().skipExistingBindingCheck();
		this.skipBindKeyword = getConfig().skipBindgenKeyword();
	}

	/** Enqueue <code>element</code> even if it was written during a previous compilation run. */
	public void enqueueForcefully(TypeElement element) {
		// Even when done forcefully, we can only touch elements once/round
		if (this.hasAlreadyBeenWrittenByThisCompilationRun(element)) {
			return;
		}
		this.enqueue(element);
	}

	/** Enqueue <code>element</code> only if we haven't seen it either this compilation run or during a previous compilation run. */
	public void enqueueIfNew(TypeElement element) {
		if (this.hasAlreadyBeenWrittenByThisCompilationRun(element) || this.hasAlreadyBeenWrittenByAPreviousCompilationRun(element)) {
			return;
		}
		this.enqueue(element);
	}

	/** Generates bindings for elements in the queue unless it is empty. */
	public void processQueue() {
		while (this.queue.size() != 0) {
			new BindingClassGenerator(this, this.queue.remove(0)).generate();
		}
	}

	/** Creates the <code>bindgen.BindKeyword</code> file unless disabled. */
	public void updateBindKeywordClass() {
		if (this.skipBindKeyword) {
			return;
		}
		new BindKeywordGenerator(this).generate(this.written);
	}

	/** Outputs <code>message</code> to System.out, mostly useful for Debug As Eclipse/javac debugging. */
	public void log(String message) {
		if (this.logEnabled) {
			System.out.println(message + " in " + this);
		}
	}

	private void enqueue(TypeElement element) {
		this.queue.add(element);
		this.written.add(element.toString());
	}

	/**
	 * Whether this compilation run has already seen <code>element</code>.
	 *
	 * We recursively walk into bindings, so first check our in-memory set of what we've
	 * seen so far this compilation run. Eclipse creates a new processor instance every time
	 * the user hits save, so this only has things from this compilation run.
	 */
	private boolean hasAlreadyBeenWrittenByThisCompilationRun(TypeElement element) {
		return this.written.contains(element.toString());
	}

	/**
	 * Whether a previous compilation run has already see <code>element</code>
	 * 
	 * If we haven't seen it this compilation run, see if we've already output awhile ago (e.g.
	 * last time they hit save), in which case we can skip it. If we really needed to do this
	 * element again, Eclipse would have deleted our last output and this check wouldn't find
	 * anything. But this does not work for javac, so we don't do it if told not to. Would be
	 * nice to auto-detect javac.
	 */
	private boolean hasAlreadyBeenWrittenByAPreviousCompilationRun(TypeElement element) {
		if (this.skipExistingBindingCheck) {
			return false;
		}
		try {
			ClassName bindingClassName = new BoundClass(element.asType()).getBindingClassName();
			FileObject fo = getFiler().getResource(
				StandardLocation.SOURCE_OUTPUT,
				bindingClassName.getPackageName(),
				bindingClassName.getSimpleName() + ".java");
			return fo.getLastModified() > 0; // exists already
		} catch (IOException io) {
			getMessager().printMessage(Kind.ERROR, io.getMessage());
			return false;
		}
	}

}
