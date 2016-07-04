package com.tegnercodes.flexio.pluginsystem;

/**
 * CyclicDependencyException will be thrown if a cyclic dependency is detected.
 */
class CyclicDependencyException extends PluginException {

	private static final long serialVersionUID = 1L;

	public CyclicDependencyException(String message) {
		super(message);
	}

}
