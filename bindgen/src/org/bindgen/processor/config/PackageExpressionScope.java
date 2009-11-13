package org.bindgen.processor.config;

import joist.util.Join;

import org.bindgen.processor.util.ClassName;

/**
 * A {@link ClassName} scope that can filter packages based on a string expression.
 * 
 * The expression specifies all beginning parts of allowed packages in a comma-separated string, eg {@code com.myapp.customers.domain,com.myapp.users.domain} which will allow any ClassName that is in or under the specified packages.

 * @author igor.vaynberg
 */
// TODO unit test
public class PackageExpressionScope implements Scope<ClassName> {

	private final String[] expressions;

	public PackageExpressionScope(String packageMask) {
		this.expressions = packageMask.split(",");
		for (int i = 0; i < this.expressions.length; i++) {
			this.expressions[i] = this.expressions[i].trim();
			if (!this.expressions[i].endsWith(".")) {
				this.expressions[i] = this.expressions[i] + ".";
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean includes(ClassName name) {
		final String packageName = name.getPackageName() + ".";
		for (String expression : this.expressions) {
			if (packageName.startsWith(expression)) {
				return true;
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new StringBuilder("[")
			.append(this.getClass().getSimpleName())
			.append(" expression=")
			.append(Join.join(this.expressions, ","))
			.append("]")
			.toString();
	}

}
