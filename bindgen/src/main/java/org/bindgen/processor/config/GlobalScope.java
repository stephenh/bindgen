package org.bindgen.processor.config;

/**
 * A {@link Scope} that allows everything
 * 
 * @author igor.vaynberg
 *
 * @param <T> type of scope objects
 */
public class GlobalScope<T> implements Scope<T> {

	@Override
	public boolean includes(T object) {
		return true;
	}

}
