package com.tegnercodes.flexio.pluginsystem;

/**
 * Overwrite classes directories to "target/classes" and lib directories to "target/lib".
 */
public class DevelopmentPluginClasspath extends PluginClasspath {

	private static final String DEVELOPMENT_CLASSES_DIRECTORY = "target/classes";
	private static final String DEVELOPMENT_LIB_DIRECTORY = "target/lib";

	public DevelopmentPluginClasspath() {
		super();
	}

	@Override
	protected void addResources() {
		classesDirectories.add(DEVELOPMENT_CLASSES_DIRECTORY);
		libDirectories.add(DEVELOPMENT_LIB_DIRECTORY);
	}


}
