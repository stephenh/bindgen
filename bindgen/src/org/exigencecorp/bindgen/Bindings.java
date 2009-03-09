package org.exigencecorp.bindgen;

public class Bindings {

    /** @return true if the bindings resolve to the same property */
    public static boolean areForSameProperty(Binding<?> b1, Binding<?> b2) {
        while (b1 != null && b2 != null) {
            if (b1 == b2) {
                return true;
            }
            boolean oneIsLast = b1.getParentBinding() == null;
            boolean twoIsLast = b2.getParentBinding() == null;
            boolean eitherEnds = oneIsLast || twoIsLast;
            boolean pathDiveragesByType = !eitherEnds && b1.getParentBinding().getType() != b2.getParentBinding().getType();
            if ((eitherEnds || pathDiveragesByType) && b1.get() == b2.get()) {
                return true;
            }
            b1 = b1.getParentBinding();
            b2 = b2.getParentBinding();
        }
        return false;
    }

}
