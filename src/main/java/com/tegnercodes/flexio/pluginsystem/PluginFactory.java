package com.tegnercodes.flexio.pluginsystem;

/**
 * Creates a plugin instance.
 */
public interface PluginFactory {

    Plugin create(PluginWrapper pluginWrapper);

}
