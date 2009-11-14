package org.bindgen.processor.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

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

	private static final String SCOPE_PARAM = "org.bindgen.scope";
	private final Map<String, String> options = new HashMap<String, String>();
	private final Scope<ClassName> bindingScope;

	public BindgenConfig(ProcessingEnvironment env) {
		this.setDefaultOptions();
		this.setUserOptions(env);
		this.bindingScope = this.getBindingScope();
	}

	public boolean shouldGenerateBindingFor(TypeElement type) {
		return this.shouldGenerateBindingFor(new ClassName(type.getQualifiedName().toString()));
	}

	public boolean shouldGenerateBindingFor(ClassName name) {
		return this.bindingScope.includes(name);
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
	private void setDefaultOptions() {
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

	private void setUserOptions(ProcessingEnvironment env) {
		for (Map.Entry<String, String> entry : env.getOptions().entrySet()) {
			this.options.put(entry.getKey(), entry.getValue());
		}
	}

}
