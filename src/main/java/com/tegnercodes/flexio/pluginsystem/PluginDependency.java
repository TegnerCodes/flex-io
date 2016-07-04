package com.tegnercodes.flexio.pluginsystem;

public class PluginDependency {

    private String pluginId;
    private String pluginVersionSupport = "*";

    public PluginDependency(String dependency) {
        int index = dependency.indexOf('@');
        if (index == -1) {
            this.pluginId = dependency;
        } else {

            this.pluginId = dependency.substring(0, index);
            if (dependency.length() > index + 1) {
                this.pluginVersionSupport = dependency.substring(index + 1);
            }
        }
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getPluginVersionSupport() {
        return pluginVersionSupport;
    }

    @Override
    public String toString() {
        return "PluginDependency [pluginId=" + pluginId + ", pluginVersionSupport=" + pluginVersionSupport + "]";
    }

}
