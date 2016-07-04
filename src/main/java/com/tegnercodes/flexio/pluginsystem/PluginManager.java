package com.tegnercodes.flexio.pluginsystem;

import java.io.File;
import java.util.List;

import com.github.zafarkhaja.semver.Version;
import com.tegnercodes.flexio.eventsystem.EventManager;

/**
 * Provides the functionality for plugin management such as load,
 * start and stop the plugins.
 */
public interface PluginManager {

    /**
     * Retrieves all plugins.
     */
    List<PluginWrapper> getPlugins();

    /**
     * Retrieves all plugins with this state.
     */
    List<PluginWrapper> getPlugins(PluginState pluginState);

    /**
     * Retrieves all resolved plugins (with resolved dependency).
     */
  	List<PluginWrapper> getResolvedPlugins();

	/**
	 * Retrieves all unresolved plugins (with unresolved dependency).
	 */
  	List<PluginWrapper> getUnresolvedPlugins();

    /**
     * Retrieves all started plugins.
     */
    List<PluginWrapper> getStartedPlugins();

    /**
     * Retrieves the plugin with this id.
     *
     * @param pluginId
     * @return the plugin
     */
    PluginWrapper getPlugin(String pluginId);

    /**
     * Load plugins.
     */
    void loadPlugins();

    /**
     * Load a plugin.
     *
     * @param pluginArchiveFile
     * @return the pluginId of the installed plugin or null
     */
	String loadPlugin(File pluginArchiveFile);

    /**
     * Start all active plugins.
     */
    void startPlugins();

    /**
     * Start the specified plugin and it's dependencies.
     *
     * @return the plugin state
     */
    PluginState startPlugin(String pluginId);

    /**
     * Stop all active plugins.
     */
    void stopPlugins();

    /**
     * Stop the specified plugin and it's dependencies.
     *
     * @return the plugin state
     */
    PluginState stopPlugin(String pluginId);

    /**
     * Unload a plugin.
     *
     * @param pluginId
     * @return true if the plugin was unloaded
     */
    boolean unloadPlugin(String pluginId);

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

    /**
     * Deletes a plugin.
     *
     * @param pluginId
     * @return true if the plugin was deleted
     */
    boolean deletePlugin(String pluginId);

	PluginClassLoader getPluginClassLoader(String pluginId);

    /**
	 * The event manager. Must currently be either DEVELOPMENT or DEPLOYMENT.
	 */
EventManager getEventManager();

	/**
	 * The runtime mode. Must currently be either DEVELOPMENT or DEPLOYMENT.
	 */
	RuntimeMode getRuntimeMode();

    /**
     * Retrieves the {@link PluginWrapper} that loaded the given class 'clazz'.
     */
    PluginWrapper whichPlugin(Class<?> clazz);

    void addPluginStateListener(PluginStateListener listener);

    void removePluginStateListener(PluginStateListener listener);

    /**
     * Set the system version.  This is used to compare against the plugin
     * requires attribute.  The default system version is 0.0.0 which
     * disables all version checking.
     *
     * @default 0.0.0
     * @param version
     */
    void setSystemVersion(Version version);

    /**
     * Returns the system version.
     *
     * * @return the system version
     */
    Version getSystemVersion();

}
