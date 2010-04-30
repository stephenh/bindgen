package org.bindgen.processor.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.bindgen.binding.AbstractBinding;
import org.bindgen.processor.CurrentEnv;
import org.bindgen.processor.util.ClassName;
import org.bindgen.processor.util.ConfUtil;

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
		return this.isEnabled("log");
	}

	public boolean skipBindKeyword() {
		return this.isEnabled("skipBindKeyword");
	}

	public boolean skipExistingBindingCheck() {
		String explicit = this.options.get("skipExistingBindingCheck");
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
		if (pn.isEmpty()) {
			return cn.getSimpleName();
		} else {
			return pn + "." + cn.getSimpleName();
		}
	}

	/** @return the fully qualified name of the super class of all Bindings - useful for integration */
	public String bindingPathSuperClassName() {
		return this.options.get("bindingPathSuperClass");
	}

	/** @return a list of class names to match void methods against for callable bindings */
	public String[] blockTypesToAttempt() {
		String attempts = this.options.get("blockTypes");
		if (attempts == null) {
			attempts = "java.lang.Runnable";
		} else {
			attempts += ",java.lang.Runnable";
		}
		return attempts.split(",");
	}

	/** @return whether the {@code @Generated} annotations should be added to the source output */
	public boolean skipGeneratedTimestamps() {
		return this.isEnabled("skipGeneratedTimestamps");
	}

	/** @return whether the field/method {@code name} of {@code element} should be skipped */
	public boolean skipAttribute(Element element, String name) {
		return this.isEnabled("skipAttribute." + element.toString() + "." + name);
	}

	/** @return the type parameter to fill in for a raw type or null */
	public String fixedRawType(Element element, String name) {
		return this.options.get("fixRawType." + element.toString() + "." + name);
	}

	private boolean isEnabled(String key) {
		return "true".equals(this.options.get(key));
	}

	private Scope<ClassName> getBindingScope() {
		final String scopeExpression = this.options.get(SCOPE_PARAM);
		if (scopeExpression != null && scopeExpression.trim().length() > 0) {
			return new PackageExpressionScope(scopeExpression);
		} else {
			return new GlobalScope<ClassName>();
		}
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
		this.options.put("bindingPathSuperClass", AbstractBinding.class.getName());
	}

	private void loadBindgenDotProperties(ProcessingEnvironment env) {
		this.options.putAll(ConfUtil.loadProperties(env, "bindgen.properties"));
	}

	private void loadAptKeyValueOptions(ProcessingEnvironment env) {
		for (Map.Entry<String, String> entry : env.getOptions().entrySet()) {
			this.options.put(entry.getKey(), entry.getValue());
		}
	}

}
