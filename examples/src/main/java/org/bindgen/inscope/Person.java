package org.bindgen.inscope;

import org.bindgen.Bindable;
import org.bindgen.binding.GenericObjectBindingPath;
import org.bindgen.outofscope.Address;

/**
 * A simple class to test scoping.
 * 
 * the generated binding of {@link #address} field should be a {@link GenericObjectBindingPath} because {@link Address} type is out of scope
 * 
 * @author igor.vaynberg
 *
 */
@Bindable
public class Person {
	public Address address;
}
