package com.tegnercodes.flexio.pluginsystem;

/**
 * The default implementation for PluginDescriptorFinder.
 * Now, this class it's a "link" to {@link com.tegnercodes.flexio.pluginsystem.ManifestPluginDescriptorFinder}.
 */
public class DefaultPluginDescriptorFinder extends ManifestPluginDescriptorFinder {

	public DefaultPluginDescriptorFinder(PluginClasspath pluginClasspath) {
		super(pluginClasspath);
	}

}
