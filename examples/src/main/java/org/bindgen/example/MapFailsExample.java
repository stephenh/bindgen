package org.bindgen.example;

import java.util.Map;

import org.bindgen.Bindable;

@Bindable
public class MapFailsExample {

	// Eclipse was failing on this due to our resolveTypeVarIfPossible
	// routine. We've since hacked around it (hopefully).
	public Map<String, String> map;

}
