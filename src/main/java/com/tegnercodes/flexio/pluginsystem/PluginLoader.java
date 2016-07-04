package com.tegnercodes.flexio.pluginsystem;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

import com.tegnercodes.flexio.pluginsystem.util.DirectoryFileFilter;
import com.tegnercodes.flexio.pluginsystem.util.JarFileFilter;

/**
 * Load all information needed by a plugin.
 * This means add all jar files from 'lib' directory, 'classes' to classpath.
 * It's a class for only the internal use.
 */
class PluginLoader {

    /*
     * The plugin repository.
     */
    private File pluginRepository;

    private PluginClasspath pluginClasspath;
    private PluginClassLoader pluginClassLoader;

    public PluginLoader(File pluginRepository, PluginClassLoader pluginClassLoader, PluginClasspath pluginClasspath) {
        this.pluginRepository = pluginRepository;
        this.pluginClassLoader = pluginClassLoader;
        this.pluginClasspath = pluginClasspath;
    }

    public boolean load() {
        return loadClassesAndJars();
    }

	private boolean loadClassesAndJars() {
       return loadClasses() && loadJars();
    }

    private boolean loadClasses() {
    	List<String> classesDirectories = pluginClasspath.getClassesDirectories();

    	// add each classes directory to plugin class loader
    	for (String classesDirectory : classesDirectories) {
	        // make 'classesDirectory' absolute
	        File file = new File(pluginRepository, classesDirectory).getAbsoluteFile();

	        if (file.exists() && file.isDirectory()) {
	            try {
	                pluginClassLoader.addURL(file.toURI().toURL());
	            } catch (MalformedURLException e) {
	                e.printStackTrace();
	                return false;
	            }
	        }
    	}

        return true;
    }

    /**
     * Add all *.jar files from lib directories to class loader.
     */
    private boolean loadJars() {
    	List<String> libDirectories = pluginClasspath.getLibDirectories();

    	// add each jars directory to plugin class loader
    	for (String libDirectory : libDirectories) {
	        // make 'libDirectory' absolute
	        File file = new File(pluginRepository, libDirectory).getAbsoluteFile();

	        // collect all jars from current lib directory in jars variable
	        Vector<File> jars = new Vector<>();
	        getJars(jars, file);
	        for (File jar : jars) {
	            try {
	                pluginClassLoader.addURL(jar.toURI().toURL());
	            } catch (MalformedURLException e) {
	                e.printStackTrace();
	                System.err.println(e.getMessage());
	                e.printStackTrace();
	                return false;
	            }
	        }
    	}

        return true;
    }

    private void getJars(Vector<File> bucket, File file) {
        FileFilter jarFilter = new JarFileFilter();
        FileFilter directoryFilter = new DirectoryFileFilter();

        if (file.exists() && file.isDirectory() && file.isAbsolute()) {
            File[] jars = file.listFiles(jarFilter);
            for (int i = 0; (jars != null) && (i < jars.length); ++i) {
                bucket.addElement(jars[i]);
            }

            File[] directories = file.listFiles(directoryFilter);
            for (int i = 0; (directories != null) && (i < directories.length); ++i) {
                File directory = directories[i];
                getJars(bucket, directory);
            }
        }
    }

}
