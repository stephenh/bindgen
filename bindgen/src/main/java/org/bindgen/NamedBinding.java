package org.bindgen;

/**
 * Denotes a named field property, method property, or method callable binding.
 *
 * For properties (fields and getters), this is the name of the property. E.g.:
 *
 * <code>
 *     @Bindable
 *     public class Foo {
 *          public String bar;
 *     }
 * </code>
 *
 * {@code FooBinding.bar()} will implement {@link NamedBinding} and
 * {@link #getName()} will return "bar".
 * 
 * For method callables, this is the name of the method. E.g.:
 *
 * <code>
 *     @Bindable
 *     public class Foo {
 *         public void bar() {
 *         }
 *     }
 * </code>
 *
 * {@code FooBinding.bar()} will implement {@link Runnable} as well
 * as {@link NamedBinding}, and {@link #getName()} will return "bar"
 * was well.
 */
public interface NamedBinding {

	String getName();

}
