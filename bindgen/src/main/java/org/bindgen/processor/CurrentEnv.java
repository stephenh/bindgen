package org.bindgen.processor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.bindgen.processor.config.BindgenConfig;

/** Provides static helper methods to get at the current {@link ProcessingEnvironment}.
 *
 * The {@link ProcessingEnvironment} used to get passed around as a method parameter,
 * but a whole lot of places need it, so putting it in one static location cut down
 * on the parameter cruft.
 *
 * This also installs several default options, specifically fixRawTypes and
 * skipAttribute settings to remove any warnings from pre-1.5 classes in the
 * {@code javax.servlet} classes.
 */
public class CurrentEnv {

	private static ProcessingEnvironment current;
	private static BindgenConfig config;

	public static void set(ProcessingEnvironment env) {
		current = env;
		config = new BindgenConfig(env);
	}

	public static ProcessingEnvironment get() {
		return current;
	}

	public static BindgenConfig getConfig() {
		return config;
	}

	public static Filer getFiler() {
		return current.getFiler();
	}

	public static Messager getMessager() {
		return current.getMessager();
	}

	public static Elements getElementUtils() {
		return current.getElementUtils();
	}

	public static Types getTypeUtils() {
		return current.getTypeUtils();
	}

}
