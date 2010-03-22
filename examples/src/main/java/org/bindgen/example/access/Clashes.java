package org.bindgen.example.access;

import org.bindgen.Bindable;

@Bindable
public class Clashes {

	public String a;

	public String getA() {
		return "from getter " + this.a;
	}

	public void setA(String a) {
		this.a = a + " by setter";
	}

	public String a() {
		return "from prefixless " + this.a;
	}

	public Clashes a(String a) {
		this.a = a + " by prefixless";
		return this;
	}

}
