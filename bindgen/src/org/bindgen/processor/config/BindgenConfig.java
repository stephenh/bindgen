package org.bindgen.processor.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.JavaFileManager.Location;

import org.bindgen.processor.CurrentEnv;
import org.bindgen.processor.util.ClassName;

/**
 * Bindgen configuration.
 * 
 * This class encapsulates all code that is responsible for making
 * decisions that can be affected by configuration options.
 * 
 * @author igor.vaynberg
 */
public class BindgenConfig {

	private static final String SCOPE_PARAM = "scope";
	private final Map<String, String> options = new HashMap<String, String>();
	private final Scope<ClassName> bindingScope;

	public BindgenConfig(ProcessingEnvironment env) {
		this.loadDefaultOptions();
		this.loadAptKeyValueOptions(env);
		this.loadBindgenDotProperties(env);
		this.bindingScope = this.getBindingScope();
	}

	public boolean shouldGenerateBindingFor(TypeElement type) {
		return this.shouldGenerateBindingFor(new ClassName(type.getQualifiedName().toString()));
	}

	public boolean shouldGenerateBindingFor(ClassName name) {
		return this.bindingScope.includes(name);
	}

	public boolean logEnabled() {
		return this.isEnabled("bindgen.log");
	}

	public boolean skipBindgenKeyword() {
		return this.isEnabled("bindgen.skipBindgenKeyword");
	}

	public boolean skipExistingBindingCheck() {
		String explicit = this.options.get("bindgen.skipExistingBindingCheck");
		if (explicit != null) {
			return "true".equals(explicit);
		}
		// javac doesn't like skipping existing bindings , so default to true if in javac
		return CurrentEnv.get().getClass().getName().startsWith("com.sun");
	}

	public String baseNameForBinding(ClassName cn) {
		String pn = cn.getPackageName();
		if (pn.startsWith("java.") || pn.startsWith("javax.")) {
			pn = "org.bindgen." + pn;
		}
		return pn + "." + cn.getSimpleName();
	}

	public String getOption(String key) {
		return this.options.get(key);
	}

	private boolean isEnabled(String key) {
		return "true".equals(this.options.get(key));
	}

	private Scope<ClassName> getBindingScope() {
		final Scope<ClassName> bindingScope;
		final String scopeExpression = this.options.get(SCOPE_PARAM);
		if (scopeExpression != null && scopeExpression.trim().length() > 0) {
			bindingScope = new PackageExpressionScope(scopeExpression);
		} else {
			bindingScope = new GlobalScope<ClassName>();
		}
		return bindingScope;
	}

	// Default properties--this is ugly, but I could not get a bindgen.properties to be found on the classpath
	private void loadDefaultOptions() {
		this.options.put("fixRawType.javax.servlet.ServletConfig.initParameterNames", "String");
		this.options.put("fixRawType.javax.servlet.ServletContext.attributeNames", "String");
		this.options.put("fixRawType.javax.servlet.ServletContext.initParameterNames", "String");
		this.options.put("fixRawType.javax.servlet.ServletRequest.attributeNames", "String");
		this.options.put("fixRawType.javax.servlet.ServletRequest.parameterNames", "String");
		this.options.put("fixRawType.javax.servlet.ServletRequest.locales", "Locale");
		this.options.put("fixRawType.javax.servlet.ServletRequest.parameterMap", "String, String[]");
		this.options.put("fixRawType.javax.servlet.http.HttpServletRequest.headerNames", "String");
		this.options.put("fixRawType.javax.servlet.http.HttpSession.attributeNames", "String");
		this.options.put("skipAttribute.javax.servlet.http.HttpSession.sessionContext", "true");
		this.options.put("skipAttribute.javax.servlet.http.HttpServletRequest.requestedSessionIdFromUrl", "true");
		this.options.put("skipAttribute.javax.servlet.ServletContext.servletNames", "true");
		this.options.put("skipAttribute.javax.servlet.ServletContext.servlets", "true");
		this.options.put("skipAttribute.java.lang.Object.getClass", "true");
		this.options.put("skipAttribute.java.lang.Object.notify", "true");
		this.options.put("skipAttribute.java.lang.Object.notifyAll", "true");
	}

	private void loadBindgenDotProperties(ProcessingEnvironment env) {
		// Eclipse, ant, and maven all act a little differently here, so try both source and class output
		File bindgenProperites = null;
		for (Location location : new Location[] { StandardLocation.SOURCE_OUTPUT, StandardLocation.CLASS_OUTPUT }) {
			bindgenProperites = this.resolveBindgenPropertiesIfExists(location, env);
			if (bindgenProperites != null) {
				break;
			}
		}
		if (bindgenProperites != null) {
			Properties p = new Properties();
			try {
				p.load(new FileInputStream(bindgenProperites));
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (Map.Entry<Object, Object> entry : p.entrySet()) {
				this.options.put((String) entry.getKey(), (String) entry.getValue());
			}
		}
	}

	private void loadAptKeyValueOptions(ProcessingEnvironment env) {
		for (Map.Entry<String, String> entry : env.getOptions().entrySet()) {
			this.options.put(entry.getKey(), entry.getValue());
		}
	}

	/** Finds a {@code bindgen.properties} file.
	 *
	 * This uses a heuristic because in Eclipse we will not know what our
	 * working directory is (it is wherever Eclipse was started from), so
	 * project/workspace-relative paths will not work.
	 *
	 * As far as passing in a bindgen.properties path as a {@code -Afile=path}
	 * setting, Eclipse also lacks any {@code ${basepath}}-type interpolation
	 * in its APT key/value pairs (like Ant would be able to do). So only fixed
	 * values are accepted, meaning an absolute path, which would be too tied
	 * to any one developer's particular machine.
	 *
	 * The one thing the APT API gives us is the CLASS_OUTPUT (e.g. bin/apt).
	 * So we start there and walk up parent directories looking for
	 * {@code bindgen.properties} files.
	 */
	private File resolveBindgenPropertiesIfExists(Location location, ProcessingEnvironment env) {
		// Find a dummy /bin/apt/dummy.txt path to start at
		final String dummyPath;
		try {
			// We don't actually create this, we just want its URI
			FileObject dummyFileObject = env.getFiler().getResource(location, "", "dummy.txt");
			dummyPath = dummyFileObject.toUri().toString().replaceAll("file:", "");
		} catch (IOException e1) {
			return null;
		}

		// Walk up looking for a bindgen.properties
		File current = new File(dummyPath).getParentFile();
		while (current != null) {
			File possible = new File(current, "bindgen.properties");
			if (possible.exists()) {
				return possible;
			}
			current = current.getParentFile();
		}

		// Before giving up, try just grabbing it from the current directory
		File possible = new File("bindgen.properties");
		if (possible.exists()) {
			return possible;
		}

		// No bindgen.properties found
		return null;
	}

}
