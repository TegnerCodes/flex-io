package com.tegnercodes.flexio.pluginsystem.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This filter providing conditional AND logic across a list of
 * file filters. This filter returns <code>true</code> if all filters in the
 * list return <code>true</code>. Otherwise, it returns <code>false</code>.
 * Checking of the file filter list stops when the first filter returns
 * <code>false</code>.
 */
public class AndFileFilter implements FileFilter {

    /** The list of file filters. */
    private List<FileFilter> fileFilters;

    public AndFileFilter() {
        this(new ArrayList<FileFilter>());
    }

    public AndFileFilter(FileFilter... fileFilters) {
        this(Arrays.asList(fileFilters));
    }

    public AndFileFilter(List<FileFilter> fileFilters) {
        this.fileFilters = new ArrayList<>(fileFilters);
    }

    public AndFileFilter addFileFilter(FileFilter fileFilter) {
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
            return false;
        }

        for (FileFilter fileFilter : this.fileFilters) {
            if (!fileFilter.accept(file)) {
                return false;
            }
        }

        return true;
    }

}
