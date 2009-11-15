package org.bindgen.inscope;

import org.bindgen.Bindable;
import org.bindgen.binding.GenericObjectBindingPath;
import org.bindgen.outofscope.AddressOut;

/**
 * A simple class to test scoping.
 * 
 * The generated binding of the {@link #addressOut} field should be a
 * {@link GenericObjectBindingPath} because the {@link AddressOut} type
 * is out of scope.
 *
 * The generated binding of the {@link #addressIn} field should be a
 * {@link AddressInBinding} because the {@link AddressIn} type is
 * in scope.
 * 
 * @author igor.vaynberg
 *
 */
@Bindable
public class Person {
	public AddressOut addressOut;
	public AddressIn addressIn;
}
