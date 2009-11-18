package org.bindgen.processor.access;

import org.bindgen.Bindable;

@Bindable
public class AccessBean {
	public String publicField;
	
	@Bindable
	protected String protectedField;

	protected String protectedField2;

	@Bindable
	String packageField;
	@Bindable
	private String privateField;

	private String publicMethod;
	private String protectedMethod;
	private String protectedMethod2;
	private String packageMethod;
	private String privateMethod;

	public String getPublicMethod() {
		return this.publicMethod;
	}

	public void setPublicMethod(String publicMethod) {
		this.publicMethod = publicMethod;
	}

	@Bindable
	protected String getProtectedMethod() {
		return this.protectedMethod;
	}

	@Bindable
	protected void setProtectedMethod(String protectedMethod) {
		this.protectedMethod = protectedMethod;
	}

	protected String getProtectedMethod2() {
		return this.protectedMethod2;
	}

	protected void setProtectedMethod2(String protectedMethod2) {
		this.protectedMethod2 = protectedMethod2;
	}

	@Bindable
	String getPackageMethod() {
		return this.packageMethod;
	}

	@Bindable
	void setPackageMethod(String packageMethod) {
		this.packageMethod = packageMethod;
	}

	@Bindable
	private String getPrivateMethod() {
		return this.privateMethod;
	}

	@Bindable
	private void setPrivateMethod(String privateMethod) {
		this.privateMethod = privateMethod;
	}

}
