package com.tegnercodes.flexio.pluginsystem;

/**
 * A wrapper over plugin instance.
 */
public class PluginWrapper {

    PluginManager pluginManager;
	PluginDescriptor descriptor;
	String pluginPath;
	PluginClassLoader pluginClassLoader;
	PluginFactory pluginFactory;
	PluginState pluginState;
	RuntimeMode runtimeMode;
    Plugin plugin; // cache

	public PluginWrapper(PluginManager pluginManager, PluginDescriptor descriptor, String pluginPath, PluginClassLoader pluginClassLoader) {
        this.pluginManager = pluginManager;
		this.descriptor = descriptor;
		this.pluginPath = pluginPath;
		this.pluginClassLoader = pluginClassLoader;

		pluginState = PluginState.CREATED;
	}

    /**
     * Returns the plugin manager.
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * Returns the plugin descriptor.
     */
    public PluginDescriptor getDescriptor() {
    	return descriptor;
    }

    /**
     * Returns the path of this plugin relative to plugins directory.
     */
    public String getPluginPath() {
    	return pluginPath;
    }

    /**
     * Returns the plugin class loader used to load classes and resources
	 * for this plug-in. The class loader can be used to directly access
	 * plug-in resources and classes.
	 */
    public ClassLoader getPluginClassLoader() {
    	return pluginClassLoader;
    }

    public Plugin getPlugin() {
        if (plugin == null) {
            plugin = pluginFactory.create(this);
        }

        return plugin;
	}

	public PluginState getPluginState() {
		return pluginState;
	}

	public RuntimeMode getRuntimeMode() {
		return runtimeMode;
	}

    /**
     * Shortcut
     */
    public String getPluginId() {
        return getDescriptor().getPluginId();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + descriptor.getPluginId().hashCode();

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		PluginWrapper other = (PluginWrapper) obj;
		if (!descriptor.getPluginId().equals(other.descriptor.getPluginId())) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "PluginWrapper [descriptor=" + descriptor + ", pluginPath=" + pluginPath + "]";
	}

	void setPluginState(PluginState pluginState) {
		this.pluginState = pluginState;
	}

	void setRuntimeMode(RuntimeMode runtimeMode) {
		this.runtimeMode = runtimeMode;
	}

    void setPluginFactory(PluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
    }

}
