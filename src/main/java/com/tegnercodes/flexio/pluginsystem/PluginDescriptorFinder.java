package com.tegnercodes.flexio.pluginsystem;

import java.io.File;

/**
 * Find a plugin descriptor in a directory (plugin repository).
 * You can find in manifest file @see DefaultPluginDescriptorFinder,
 * xml file, properties file, java services (with ServiceLoader), etc.
 */
public interface PluginDescriptorFinder {

	PluginDescriptor find(File pluginRepository) throws PluginException;

}
