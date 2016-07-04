package com.tegnercodes.flexio.pluginsystem.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This filter providing conditional OR logic across a list of
 * file filters. This filter returns <code>true</code> if one filter in the
 * list return <code>true</code>. Otherwise, it returns <code>false</code>.
 * Checking of the file filter list stops when the first filter returns
 * <code>true</code>.
 */
public class OrFileFilter implements FileFilter {

    /** The list of file filters. */
    private List<FileFilter> fileFilters;

    public OrFileFilter() {
        this(new ArrayList<FileFilter>());
    }

    public OrFileFilter(FileFilter... fileFilters) {
        this(Arrays.asList(fileFilters));
    }

    public OrFileFilter(List<FileFilter> fileFilters) {
        this.fileFilters = new ArrayList<>(fileFilters);
    }

    public OrFileFilter addFileFilter(FileFilter fileFilter) {
        fileFilters.add(fileFilter);

        return this;
    }

    public List<FileFilter> getFileFilters() {
        return Collections.unmodifiableList(fileFilters);
    }

    public boolean removeFileFilter(FileFilter fileFilter) {
        return fileFilters.remove(fileFilter);
    }

    public void setFileFilters(List<FileFilter> fileFilters) {
        this.fileFilters = new ArrayList<>(fileFilters);
    }

    @Override
    public boolean accept(File file) {
        if (this.fileFilters.size() == 0) {
            return true;
        }

        for (FileFilter fileFilter : this.fileFilters) {
            if (fileFilter.accept(file)) {
                return true;
            }
        }

        return false;
    }

}
