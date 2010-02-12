package org.bindgen.example.inheritance;

import org.bindgen.Bindable;
import org.bindgen.example.inheritance2.InterfaceAA;

@Bindable
public interface InterfaceB extends InterfaceA, InterfaceAA {
	String getFromB();
}
