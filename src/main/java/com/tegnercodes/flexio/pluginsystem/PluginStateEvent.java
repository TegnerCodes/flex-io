package com.tegnercodes.flexio.pluginsystem;

import java.util.EventObject;

public class PluginStateEvent extends EventObject {

    /**
	 * Serial id (not used for much)
	 */
	private static final long serialVersionUID = 24052016L;
	private PluginWrapper plugin;
    private PluginState oldState;

    public PluginStateEvent(PluginManager source, PluginWrapper plugin, PluginState oldState) {
        super(source);

        this.plugin = plugin;
        this.oldState = oldState;
    }

    @Override
    public PluginManager getSource() {
        return (PluginManager) super.getSource();
    }

    public PluginWrapper getPlugin() {
        return plugin;
    }

    public PluginState getPluginState() {
        return plugin.getPluginState();
    }

    public PluginState getOldState() {
        return oldState;
    }

    @Override
    public String toString() {
        return "PluginStateEvent [plugin=" + plugin.getPluginId() +
                ", newState=" + getPluginState() +
                ", oldState=" + oldState +
                ']';
    }

}
