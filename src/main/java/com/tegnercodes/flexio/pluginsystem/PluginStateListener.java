package com.tegnercodes.flexio.pluginsystem;

import java.util.EventListener;

/**
 * PluginStateListener defines the interface for an object that listens to plugin state changes.
 */
public interface PluginStateListener extends EventListener {

    /**
     * Invoked when a plugin's state (for example DISABLED, STARTED) is changed.
     */
    void pluginStateChanged(PluginStateEvent event);

}
