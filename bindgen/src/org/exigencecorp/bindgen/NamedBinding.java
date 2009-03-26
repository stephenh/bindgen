package org.exigencecorp.bindgen;

/**
 * Interface to provide the name of a binding.
 *
 * For properties (fields and getters), this is the name
 * of the property. E.g.:
 *
 *     @Bindable
 *     public class Foo {
 *          public String bar;
 *     }
 *
 * <code>FooBinding.bar()</code> will implement {@link Binding} and
 * <code>getName()</code> will return "bar".
 * 
 * For callables (methods), this is the name of the
 * method. E.g.:
 *
 *     @Bindable
 *     public class Foo {
 *         public void bar() {
 *         }
 *     }
 *
 * <code>FooBinding.bar()</code> will implement {@link Runnable} as well
 * as {@link NamedBinding}, and <code>NamedBinding.getName()</code> will
 * return "bar" was well.
 */
public interface NamedBinding {

    String getName();

}
