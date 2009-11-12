package org.bindgen.example.blocks;

import org.bindgen.Bindable;

@Bindable
public class RunnableExample {

	private boolean stuffDone;

	public void doStuff() {
		this.stuffDone = true;
	}

	public String doBarIsIgnored() {
		return null;
	}

	public void doZazIsIgnored(String zoz) {
	}

	public boolean isStuffDone() {
		return this.stuffDone;
	}

}
