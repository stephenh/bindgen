package org.bindgen;

public class Bindings {

	/**
	 * Determines if separate binding instances are bound to the same underlying property.
	 *
	 * Note that the "same underlying property" is somewhat ambiguous. Where the current
	 * heuristic works is for one-level look back to a non-value/reference object.
	 *
	 * For example, domainObject1.name(). Each binding will be a StringBinding, and each
	 * parent will be domainObject1, so they will resolve the same. This also works if
	 * one binding was gotten by myPage.domainObject().name() and another via
	 * new DomainObjectBinding(domainObject).name().
	 *
	 * Where this heuristic fails is if the one-level look back is a value object, e.g.
	 * domainObject1.name().empty() and domainObject2.name().empty() where both name's
	 * are the same instance (e.g. == not just equals). Here we expect bindings based
	 * off of domainObject1 and domainObject2 to be "different", but since the same
	 * instance of name gets in the way, the current implementation returns true.
	 *
	 * In practice, I think this will be okay, especially since value-object are typically
	 * read-only and so unlikely to be at the first-level look position in the path. Though
	 * if we could distinguish between value and value-non objects and walk to the first
	 * non-value object, that would be cool.
	 *
	 * @return true if the bindings resolve to the same property
	 */
	public static boolean areForSameProperty(Binding<?> b1, Binding<?> b2) {
		if (b1 == null || b2 == null || b1.getParentBinding() == null || b1.getParentBinding().get() == null) {
			return false;
		}
		return b1.getParentBinding().get() == b2.getParentBinding().get();
	}

}
