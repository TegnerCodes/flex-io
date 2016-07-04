package com.tegnercodes.flexio.pluginsystem.util;

import java.io.File;
import java.io.FileFilter;

/**
 * Filter that only accepts hidden files.
 */
public class HiddenFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		return file.isHidden();
	}

}
