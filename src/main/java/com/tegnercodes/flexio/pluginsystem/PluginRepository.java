package com.tegnercodes.flexio.pluginsystem;

import java.io.File;
import java.util.List;

/**
 * Directory whose contents are .zip files used as plugins.
 */
public interface PluginRepository {

    /**
     * List all plugin archive filed.
     *
     * @return a list of files
     */
    List<File> getPluginArchives();

    /**
     * Removes a plugin from the repository.
     *
     * @param pluginPath the plugin path
     * @return true if deleted
     */
    boolean deletePluginArchive(String pluginPath);

}
