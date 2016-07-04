package com.tegnercodes.flexio.pluginsystem;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.tegnercodes.flexio.pluginsystem.util.FileUtils;

public class DefaultPluginRepository implements PluginRepository {

    private final File directory;
    private final FileFilter filter;

    public DefaultPluginRepository(File directory, FileFilter filter) {
        this.directory = directory;
        this.filter = filter;
    }

    @Override
    public List<File> getPluginArchives() {
        File[] files = directory.listFiles(filter);

        return (files != null) ? Arrays.asList(files) : Collections.<File>emptyList();
    }

    @Override
    public boolean deletePluginArchive(String pluginPath) {
        File[] files = directory.listFiles(filter);
        if (files != null) {
            File pluginArchive = null;
            // strip prepended "/" from the plugin path
            String dirName = pluginPath.substring(1);
            // find the zip file that matches the plugin path
            for (File archive : files) {
                String name = archive.getName().substring(0, archive.getName().lastIndexOf('.'));
                if (name.equals(dirName)) {
                    pluginArchive = archive;
                    break;
                }
            }
            if (pluginArchive != null && pluginArchive.exists()) {
                return FileUtils.delete(pluginArchive);
            }
        }

        return false;
    }

}
