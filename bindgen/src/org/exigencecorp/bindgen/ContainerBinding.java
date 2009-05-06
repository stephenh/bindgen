package org.exigencecorp.bindgen;

/**
 * Interface to denote a binding that contains another type, e.g. Lists and Sets.
 *
 * This is for getting around the burden of type erasure.
 */
public interface ContainerBinding {

    Class<?> getContainedType();

}
