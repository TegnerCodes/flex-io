package com.tegnercodes.flexio.updatesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tegnercodes.flexio.pluginsystem.PluginManager;
import com.tegnercodes.flexio.pluginsystem.PluginState;
import com.tegnercodes.flexio.pluginsystem.PluginWrapper;

public class UpdateManager {

    private static final String repositoriesFile = "repositories.json";

    private List<UpdateRepository> repositories;

    public PluginManager pluginManager;

    public UpdateManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public List<UpdateRepository.PluginInfo> getAvailablePlugins() {
        List<UpdateRepository.PluginInfo> availablePlugins = new ArrayList<UpdateRepository.PluginInfo>();
        List<UpdateRepository.PluginInfo> plugins = getPlugins();
        for (UpdateRepository.PluginInfo plugin : plugins) {
            if (pluginManager.getPlugin(plugin.id) == null) {
                availablePlugins.add(plugin);
            }
        }

        return availablePlugins;
    }

    public boolean hasAvailablePlugins() {
        List<UpdateRepository.PluginInfo> plugins = getPlugins();
        for (UpdateRepository.PluginInfo plugin : plugins) {
            if (pluginManager.getPlugin(plugin.id) == null) {
                return true;
            }
        }

        return false;
    }

    public List<UpdateRepository.PluginInfo> getUpdates() {
        List<UpdateRepository.PluginInfo> updates = new ArrayList<UpdateRepository.PluginInfo>();
        List<UpdateRepository.PluginInfo> plugins = getPlugins();
        for (UpdateRepository.PluginInfo plugin : plugins) {
            PluginWrapper installedPlugin = pluginManager.getPlugin(plugin.id);
            if (installedPlugin != null) {
                Version installedVersion = installedPlugin.getDescriptor().getVersion();
                if (plugin.hasUpdate(getSystemVersion(), installedVersion)) {
                    updates.add(plugin);
                }
            }
        }

        return updates;
    }

    public boolean hasUpdates() {
        List<UpdateRepository.PluginInfo> plugins = getPlugins();
        for (UpdateRepository.PluginInfo plugin : plugins) {
            PluginWrapper installedPlugin = pluginManager.getPlugin(plugin.id);
            if (installedPlugin != null) {
                Version installedVersion = installedPlugin.getDescriptor().getVersion();
                if (plugin.hasUpdate(getSystemVersion(), installedVersion)) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<UpdateRepository.PluginInfo> getPlugins() {
        List<UpdateRepository.PluginInfo> plugins = new ArrayList<UpdateRepository.PluginInfo>();
        List<UpdateRepository> repositories = getRepositories();
        for (UpdateRepository repository : repositories) {
            plugins.addAll(repository.getPlugins());
        }

        return plugins;
    }

    public List<UpdateRepository> getRepositories() {
        if (repositories == null) {
            initRepositories();
        }

        return repositories;
    }

    public synchronized void refresh() {
        repositories = null;
    }

    public synchronized boolean installPlugin(String url) {
        File pluginArchiveFile;
        try {
            pluginArchiveFile = new FileDownloader().downloadFile(url);
        } catch (IOException e) {
        	System.err.println(e.getMessage());
            return false;
        }

        String pluginId = pluginManager.loadPlugin(pluginArchiveFile);
        PluginState state = pluginManager.startPlugin(pluginId);

        return PluginState.STARTED.equals(state);
    }

    public boolean updatePlugin(String id, String url) {
        File pluginArchiveFile;
        try {
            pluginArchiveFile = new FileDownloader().downloadFile(url);
        } catch (IOException e) {
        	System.err.println(e.getMessage());
            return false;
        }

        if (!pluginManager.deletePlugin(id)) {
            return false;
        }

        String newPluginId = pluginManager.loadPlugin(pluginArchiveFile);
        PluginState state = pluginManager.startPlugin(newPluginId);

        return PluginState.STARTED.equals(state);
    }

    public boolean uninstallPlugin(String id) {
        return pluginManager.deletePlugin(id);
    }

    private synchronized void initRepositories() {
        FileReader reader = null;
        try {
            reader = new FileReader(repositoriesFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            repositories = Collections.emptyList();
            return;
        }

        Gson gson = new GsonBuilder().create();
        UpdateRepository[] items = gson.fromJson(reader, UpdateRepository[].class);

        repositories = Arrays.asList(items);
    }

    private Version getSystemVersion() {
        return pluginManager.getSystemVersion();
    }

}
