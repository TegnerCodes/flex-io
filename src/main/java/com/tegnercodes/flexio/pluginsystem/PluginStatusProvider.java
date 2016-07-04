package com.tegnercodes.flexio.pluginsystem;

public interface PluginStatusProvider {

    /**
     * Checks if the plugin is disabled or not
     *
     * @param pluginId the plugin id
     * @return if the plugin is disabled or not
     */
    boolean isPluginDisabled(String pluginId);

    /**
     * Disables a plugin from being loaded.
     *
     * @param pluginId
     * @return true if plugin is disabled
     */
    boolean disablePlugin(String pluginId);

    /**
     * Enables a plugin that has previously been disabled.
     *
     * @param pluginId
     * @return true if plugin is enabled
     */
    boolean enablePlugin(String pluginId);

}
