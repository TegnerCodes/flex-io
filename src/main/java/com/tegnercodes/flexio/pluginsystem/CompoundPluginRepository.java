package com.tegnercodes.flexio.pluginsystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CompoundPluginRepository implements PluginRepository {

    private final PluginRepository[] repositories;

    public CompoundPluginRepository(PluginRepository... repositories) {
        this.repositories = repositories;
    }

    @Override
    public List<File> getPluginArchives() {
        List<File> file = new ArrayList<>();
        for (PluginRepository repository : repositories) {
            file.addAll(repository.getPluginArchives());
        }

        return file;
    }

    @Override
    public boolean deletePluginArchive(String pluginPath) {
        for (PluginRepository repository : repositories) {
            if (repository.deletePluginArchive(pluginPath)) {
                return true;
            }
        }

        return false;
    }

}
