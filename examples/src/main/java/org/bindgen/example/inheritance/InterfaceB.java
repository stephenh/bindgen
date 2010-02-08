package org.bindgen.example.inheritance;

import org.bindgen.Bindable;

@Bindable
public interface InterfaceB extends InterfaceA {
	String getFromB();
}
