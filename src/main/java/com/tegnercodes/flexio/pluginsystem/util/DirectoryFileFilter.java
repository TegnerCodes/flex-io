package com.tegnercodes.flexio.pluginsystem.util;

import java.io.File;
import java.io.FileFilter;

/**
 * Filter accepts files that are directories.
 */
public class DirectoryFileFilter implements FileFilter {

	@Override
    public boolean accept(File file) {
        return file.isDirectory();
    }

}
