package com.tegnercodes.flexio.pluginsystem.util;

import java.io.File;
import java.io.FileFilter;

/**
 * This filter produces a logical NOT of the filters specified.
 */
public class NotFileFilter implements FileFilter {

    private FileFilter filter;

    public NotFileFilter(FileFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean accept(File file) {
        return !filter.accept(file);
    }

}
