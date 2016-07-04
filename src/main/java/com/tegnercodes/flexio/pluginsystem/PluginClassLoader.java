package com.tegnercodes.flexio.pluginsystem;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * One instance of this class should be created by plugin manager for every available plug-in.
 * This class loader is a Parent Last ClassLoader - it loads the classes from the plugin's jars before delegating
 * to the parent class loader.
 */
public class PluginClassLoader extends URLClassLoader {

	private static final String PLUGIN_PACKAGE_PREFIX = "com.tegnercodes.flexio.pluginsystem.";

	private PluginManager pluginManager;
	private PluginDescriptor pluginDescriptor;

	public PluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent) {
		super(new URL[0], parent);

		this.pluginManager = pluginManager;
		this.pluginDescriptor = pluginDescriptor;
	}

	@Override
	public void addURL(URL url) {
		super.addURL(url);
	}

    /**
     * This implementation of loadClass uses a child first delegation model rather than the standard parent first.
     * If the requested class cannot be found in this class loader, the parent class loader will be consulted
     * via the standard ClassLoader.loadClass(String) mechanism.
     */
	@Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(className)) {
            // if the class it's a part of the plugin engine use parent class loader
            if (className.startsWith(PLUGIN_PACKAGE_PREFIX)) {
                try {
                    return getClass().getClassLoader().loadClass(className);
                } catch (ClassNotFoundException e) {
                    // try next step
                    // TODO if I uncomment below lines (the correct approach) I received ClassNotFoundException for demo (com.tegnercodes.flexio.pluginsystem.demo)
                    //                log.error(e.getMessage(), e);
                    //                throw e;
                }
            }

            // second check whether it's already been loaded
            Class<?> clazz = findLoadedClass(className);
            if (clazz != null) {
                return clazz;
            }

            // nope, try to load locally
            try {
                clazz = findClass(className);
                return clazz;
            } catch (ClassNotFoundException e) {
                // try next step
            }

            // look in dependencies
            List<PluginDependency> dependencies = pluginDescriptor.getDependencies();
            for (PluginDependency dependency : dependencies) {
                PluginClassLoader classLoader = pluginManager.getPluginClassLoader(dependency.getPluginId());
                try {
                    return classLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    // try next dependency
                }
            }

            // use the standard URLClassLoader (which follows normal parent delegation)
            return super.loadClass(className);
        }
    }

    /**
     * Load the named resource from this plugin. This implementation checks the plugin's classpath first
     * then delegates to the parent.
     *
     * @param name the name of the resource.
     * @return the URL to the resource, <code>null</code> if the resource was not found.
     */
    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if (url != null) {
            return url;
        }

        return super.getResource(name);
    }

    @Override
    public URL findResource(String name) {
        return super.findResource(name);
    }

    /**
     * Release all resources acquired by this class loader.
     * The current implementation is incomplete.
     * For now, this instance can no longer be used to load
     * new classes or resources that are defined by this loader.
     */
    public void dispose() {
        try {
            close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
