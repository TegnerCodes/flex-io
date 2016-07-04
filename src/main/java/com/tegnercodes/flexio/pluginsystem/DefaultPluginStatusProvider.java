package com.tegnercodes.flexio.pluginsystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tegnercodes.flexio.pluginsystem.util.FileUtils;

/**
 * The default implementation for PluginStatusProvider.
 */
public class DefaultPluginStatusProvider implements PluginStatusProvider {

    private final File pluginsDirectory;

    private List<String> enabledPlugins = new ArrayList<>();
    private List<String> disabledPlugins = new ArrayList<>();

    public DefaultPluginStatusProvider(File pluginsDirectory) {
        this.pluginsDirectory = pluginsDirectory;
        initialize();
    }

    private void initialize() {
        try {
            // create a list with plugin identifiers that should be only accepted by this manager (whitelist from plugins/enabled.txt file)
            enabledPlugins = FileUtils.readLines(new File(pluginsDirectory, "enabled.txt"), true);

            // create a list with plugin identifiers that should not be accepted by this manager (blacklist from plugins/disabled.txt file)
            disabledPlugins = FileUtils.readLines(new File(pluginsDirectory, "disabled.txt"), true);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean isPluginDisabled(String pluginId) {
        if (disabledPlugins.contains(pluginId)) {
            return true;
        }

        return !enabledPlugins.isEmpty() && !enabledPlugins.contains(pluginId);
    }

    @Override
    public boolean disablePlugin(String pluginId) {
        if (disabledPlugins.add(pluginId)) {
            try {
                FileUtils.writeLines(disabledPlugins, new File(pluginsDirectory, "disabled.txt"));
            } catch (IOException e) {
                System.err.println("Failed to disable plugin " + pluginId);
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean enablePlugin(String pluginId) {
        try {
            if (disabledPlugins.remove(pluginId)) {
                FileUtils.writeLines(disabledPlugins, new File(pluginsDirectory, "disabled.txt"));
            }
        } catch (IOException e) {
            System.err.println("Failed to enable plugin " + pluginId);
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
