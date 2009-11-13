package org.bindgen.processor.config;

/**
 * A representation of a scope.
 * 
 * @author igor.vaynberg
 *
 * @param <T> type of scope objects
 */
public interface Scope<T> {

	boolean includes(T object);

}
