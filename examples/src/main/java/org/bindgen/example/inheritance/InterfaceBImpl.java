package org.bindgen.example.inheritance;

public class InterfaceBImpl implements InterfaceB {

	@Override
	public String getFromA() {
		return "a";
	}

	@Override
	public String getFromB() {
		return "b";
	}

	@Override
	public String getFromAA() {
		return "aa";
	}

}
