package com.tegnercodes.flexio.pluginsystem;

import java.util.ArrayList;
import java.util.List;

/**
 * The classpath of the plugin after it was unpacked.
 * It contains classes directories and lib directories (directories that contains jars).
 * All directories are relative to plugin repository.
 * The default values are "classes" and "lib".
 */
public class PluginClasspath {

	private static final String DEFAULT_CLASSES_DIRECTORY = "classes";
	private static final String DEFAULT_LIB_DIRECTORY = "lib";

	protected List<String> classesDirectories;
	protected List<String> libDirectories;

	public PluginClasspath() {
		classesDirectories = new ArrayList<>();
		libDirectories = new ArrayList<>();

		addResources();
	}

	public List<String> getClassesDirectories() {
		return classesDirectories;
	}

	public void setClassesDirectories(List<String> classesDirectories) {
		this.classesDirectories = classesDirectories;
	}

	public List<String> getLibDirectories() {
		return libDirectories;
	}

	public void setLibDirectories(List<String> libDirectories) {
		this.libDirectories = libDirectories;
	}

	protected void addResources() {
		classesDirectories.add(DEFAULT_CLASSES_DIRECTORY);
		libDirectories.add(DEFAULT_LIB_DIRECTORY);
	}

}
