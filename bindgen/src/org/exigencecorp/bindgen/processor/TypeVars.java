package org.exigencecorp.bindgen.processor;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;

import joist.util.Join;

public class TypeVars {

    public final String generics;
    public final String genericsWithBounds;

    public TypeVars(DeclaredType t) {
        List<String> simple = new ArrayList<String>();
        List<String> args = new ArrayList<String>();
        for (TypeVariable tv : (List<TypeVariable>) t.getTypeArguments()) {
            String arg = tv.toString();
            if (tv.getUpperBound().getKind() != TypeKind.NONE && !"java.lang.Object".equals(tv.getUpperBound().toString())) {
                arg += " extends " + tv.getUpperBound().toString();
            }
            simple.add(tv.toString());
            args.add(arg);
        }
        this.generics = Join.commaSpace(simple);
        this.genericsWithBounds = Join.commaSpace(args);
    }
}
