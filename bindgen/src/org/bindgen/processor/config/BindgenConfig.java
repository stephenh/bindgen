package org.bindgen.processor.config;

import javax.lang.model.element.TypeElement;

import org.bindgen.processor.util.ClassName;

/**
 * Bindgen configuration. This class encapsulates all code that is responsible for making decisions that can be affected by configuration options.
 * 
 * @author igor.vaynberg
 */
public class BindgenConfig {

	// TODO move all configuration-dependent code into here instead of using CurrentEnv.getOptions()

	private final Scope<ClassName> bindingScope;

	public BindgenConfig(Scope<ClassName> bindingScope) {
		this.bindingScope = bindingScope;
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

}
