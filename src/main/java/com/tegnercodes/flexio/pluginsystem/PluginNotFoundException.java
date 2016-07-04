package com.tegnercodes.flexio.pluginsystem;

class PluginNotFoundException extends PluginException {

	private static final long serialVersionUID = 1L;

	private String pluginId;

	public PluginNotFoundException(String pluginId) {
		super("Plugin '" + pluginId + "' not found.");

		this.pluginId = pluginId;
	}

	public String getPluginId() {
		return pluginId;
	}

}
