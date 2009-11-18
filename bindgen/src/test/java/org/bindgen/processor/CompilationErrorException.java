package org.bindgen.processor;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

/**
 * Exception throw when compilation fails during a test case execution.
 * 
 * @author igor.vaynberg
  */
public class CompilationErrorException extends Exception {
	private static final long serialVersionUID = 1L;

	private final DiagnosticCollector<JavaFileObject> diagnosticCollector;

	/**
	 * Constructor
	 * 
	 * @param diagnosticCollector collector that contains compilation errors
	 */
	public CompilationErrorException(DiagnosticCollector<JavaFileObject> diagnosticCollector) {
		this.diagnosticCollector = diagnosticCollector;
	}

	/**
	 * @return diagnostic collector
	 */
	public DiagnosticCollector<JavaFileObject> getDiagnosticCollector() {
		return this.diagnosticCollector;
	}

}
