package org.bindgen.outofscope;

import org.bindgen.Bindable;

/**
 * A bean that is out of bindgen's scope, and so no binding should be generated
 * 
 * @author igor.vaynberg
 *
 */
@Bindable
public class Address {
	public String city;
}
