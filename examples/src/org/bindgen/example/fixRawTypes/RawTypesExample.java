package org.bindgen.example.fixRawTypes;

import java.util.Enumeration;

import org.bindgen.Bindable;

@Bindable
public class RawTypesExample {

	public Enumeration<String> fieldGiven;
	@SuppressWarnings("unchecked")
	public Enumeration fieldRaw;
	@SuppressWarnings("unchecked")
	public Enumeration fieldFixed;
	@SuppressWarnings("unchecked")
	private Enumeration methodRaw;
	@SuppressWarnings("unchecked")
	private Enumeration methodFixed;
	private Enumeration<String> methodGiven;

	@SuppressWarnings("unchecked")
	public Enumeration getMethodFixed() {
		return this.methodFixed;
	}

	@SuppressWarnings("unchecked")
	public void setMethodFixed(Enumeration e) {
		this.methodFixed = e;
	}

	@SuppressWarnings("unchecked")
	public Enumeration getMethodRaw() {
		return this.methodRaw;
	}

	@SuppressWarnings("unchecked")
	public void setMethodRaw(Enumeration e) {
		this.methodRaw = e;
	}

	public Enumeration<String> getMethodGiven() {
		return this.methodGiven;
	}

	public void setMethodGiven(Enumeration<String> given) {
		this.methodGiven = given;
	}

}
