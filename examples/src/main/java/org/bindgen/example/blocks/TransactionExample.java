package org.bindgen.example.blocks;

import org.bindgen.Bindable;

@Bindable
public class TransactionExample {

	public Boolean businessLogic(String context) {
		return "good".equals(context);
	}

	public Boolean businessLogicThatCanFail(String context) throws Exception {
		throw new Exception("I failed");
	}
}
