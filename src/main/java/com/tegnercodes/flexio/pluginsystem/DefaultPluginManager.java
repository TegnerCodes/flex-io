package com.tegnercodes.flexio.pluginsystem;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.zafarkhaja.semver.Version;
import com.github.zafarkhaja.semver.expr.Expression;
import com.tegnercodes.flexio.eventsystem.EventManager;
import com.tegnercodes.flexio.pluginsystem.util.AndFileFilter;
import com.tegnercodes.flexio.pluginsystem.util.DirectoryFileFilter;
import com.tegnercodes.flexio.pluginsystem.util.FileUtils;
import com.tegnercodes.flexio.pluginsystem.util.HiddenFilter;
import com.tegnercodes.flexio.pluginsystem.util.NameFileFilter;
import com.tegnercodes.flexio.pluginsystem.util.NotFileFilter;
import com.tegnercodes.flexio.pluginsystem.util.OrFileFilter;
import com.tegnercodes.flexio.pluginsystem.util.Unzip;
import com.tegnercodes.flexio.pluginsystem.util.ZipFileFilter;

/**
 * Default implementation of the PluginManager interface.
 */
public class DefaultPluginManager implements PluginManager {
	public static final String DEFAULT_PLUGINS_DIRECTORY = "plugins";
	public static final String DEVELOPMENT_PLUGINS_DIRECTORY = "../plugins";

    protected File pluginsDirectory;

    protected PluginDescriptorFinder pluginDescriptorFinder;

    protected PluginClasspath pluginClasspath;

    /**
     * A map of plugins this manager is responsible for (the key is the 'pluginId').
     */
    protected Map<String, PluginWrapper> plugins;

    /**
     * A map of plugin class loaders (he key is the 'pluginId').
     */
    protected Map<String, PluginClassLoader> pluginClassLoaders;

    /**
     * A relation between 'pluginPath' and 'pluginId'
     */
    protected Map<String, String> pathToIdMap;

    /**
     * A list with unresolved plugins (unresolved dependency).
     */
    protected List<PluginWrapper> unresolvedPlugins;

    /**
     * A list with resolved plugins (resolved dependency).
     */
    protected List<PluginWrapper> resolvedPlugins;

    /**
     * A list with started plugins.
     */
    protected List<PluginWrapper> startedPlugins;

    /**
     * The registered {@link PluginStateListener}s.
     */
    protected List<PluginStateListener> pluginStateListeners;

    /**
     * Cache value for the runtime mode. No need to re-read it because it wont change at
	 * runtime.
     */
    protected RuntimeMode runtimeMode;

    /**
     * The system version used for comparisons to the plugin requires attribute.
     */
    protected Version systemVersion = Version.forIntegers(1, 0, 0);

    protected PluginFactory pluginFactory;
    protected PluginStatusProvider pluginStatusProvider;
    protected DependencyResolver dependencyResolver;

    /**
     * The plugins repository.
     */
    protected PluginRepository pluginRepository;

    /**
     * The plugins directory is supplied by System.getProperty("pf4j.pluginsDir", "plugins").
     */
    public DefaultPluginManager() {
        this.pluginsDirectory = createPluginsDirectory();

        initialize();
    }

    /**
     * Constructs DefaultPluginManager which the given plugins directory.
     *
     * @param pluginsDirectory the directory to search for plugins
     */
    public DefaultPluginManager(File pluginsDirectory) {
        this.pluginsDirectory = pluginsDirectory;

        initialize();
    }

    @Override
    public void setSystemVersion(Version version) {
    	systemVersion = version;
    }

    @Override
    public Version getSystemVersion() {
    	return systemVersion;
    }

	@Override
    public List<PluginWrapper> getPlugins() {
        return new ArrayList<>(plugins.values());
    }

    @Override
    public List<PluginWrapper> getPlugins(PluginState pluginState) {
        List<PluginWrapper> plugins= new ArrayList<>();
        for (PluginWrapper plugin : getPlugins()) {
            if (pluginState.equals(plugin.getPluginState())) {
                plugins.add(plugin);
            }
        }

        return plugins;
    }

    @Override
	public List<PluginWrapper> getResolvedPlugins() {
		return resolvedPlugins;
	}

	@Override
    public List<PluginWrapper> getUnresolvedPlugins() {
		return unresolvedPlugins;
	}

	@Override
	public List<PluginWrapper> getStartedPlugins() {
		return startedPlugins;
	}

    @Override
    public PluginWrapper getPlugin(String pluginId) {
        return plugins.get(pluginId);
    }

	@Override
	public String loadPlugin(File pluginArchiveFile) {
		if ((pluginArchiveFile == null) || !pluginArchiveFile.exists()) {
			throw new IllegalArgumentException(String.format("Specified plugin %s does not exist!", pluginArchiveFile));
		}

		File pluginDirectory = null;
		try {
			pluginDirectory = expandPluginArchive(pluginArchiveFile);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		if ((pluginDirectory == null) || !pluginDirectory.exists()) {
			throw new IllegalArgumentException(String.format("Failed to expand %s", pluginArchiveFile));
		}

		try {
			PluginWrapper pluginWrapper = loadPluginDirectory(pluginDirectory);
			// TODO uninstalled plugin dependencies?
        	unresolvedPlugins.remove(pluginWrapper);
        	resolvedPlugins.add(pluginWrapper);

            firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, null));

			return pluginWrapper.getDescriptor().getPluginId();
		} catch (PluginException e) {
			System.err.println(e.getMessage());
		}

		return null;
	}

    /**
     * Start all active plugins.
     */
	@Override
    public void startPlugins() {
        for (PluginWrapper pluginWrapper : resolvedPlugins) {
            PluginState pluginState = pluginWrapper.getPluginState();
            if ((PluginState.DISABLED != pluginState) && (PluginState.STARTED != pluginState)) {
                try {
                    PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
                    System.out.println("Start plugin " + pluginDescriptor.getPluginId() + ":" + pluginDescriptor.getVersion());
                    pluginWrapper.getPlugin().start();
                    pluginWrapper.setPluginState(PluginState.STARTED);
                    startedPlugins.add(pluginWrapper);

                    firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
                } catch (PluginException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

	/**
     * Start the specified plugin and it's dependencies.
     */
    @Override
    public PluginState startPlugin(String pluginId) {
    	if (!plugins.containsKey(pluginId)) {
    		throw new IllegalArgumentException(String.format("Unknown pluginId %s", pluginId));
    	}

    	PluginWrapper pluginWrapper = getPlugin(pluginId);
    	PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
    	if (PluginState.STARTED == pluginState) {
    		return PluginState.STARTED;
    	}

        if (PluginState.DISABLED == pluginState) {
            // automatically enable plugin on manual plugin start
            if (!enablePlugin(pluginId)) {
                return pluginState;
            }
        }

        for (PluginDependency dependency : pluginDescriptor.getDependencies()) {
    		startPlugin(dependency.getPluginId());
    	}

    	try {
    		System.err.println("Start plugin " + pluginDescriptor.getPluginId() + " " + pluginDescriptor.getVersion());
    		pluginWrapper.getPlugin().start();
    		pluginWrapper.setPluginState(PluginState.STARTED);
    		startedPlugins.add(pluginWrapper);

            firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
        } catch (PluginException e) {
    		System.err.println(e.getMessage());
    		e.printStackTrace();
    	}

    	return pluginWrapper.getPluginState();
    }

    /**
     * Stop all active plugins.
     */
    @Override
    public void stopPlugins() {
    	// stop started plugins in reverse order
    	Collections.reverse(startedPlugins);
    	Iterator<PluginWrapper> itr = startedPlugins.iterator();
        while (itr.hasNext()) {
        	PluginWrapper pluginWrapper = itr.next();
            PluginState pluginState = pluginWrapper.getPluginState();
            if (PluginState.STARTED == pluginState) {
                try {
                    PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
                    System.out.println("Stop plugin " + pluginDescriptor.getPluginId() + " " + pluginDescriptor.getVersion());
                    pluginWrapper.getPlugin().stop();
                    pluginWrapper.setPluginState(PluginState.STOPPED);
                    itr.remove();

                    firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
                } catch (PluginException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Stop the specified plugin and it's dependencies.
     */
    @Override
    public PluginState stopPlugin(String pluginId) {
        return stopPlugin(pluginId, true);
    }

    private PluginState stopPlugin(String pluginId, boolean stopDependents) {
    	if (!plugins.containsKey(pluginId)) {
    		throw new IllegalArgumentException(String.format("Unknown pluginId %s", pluginId));
    	}

    	PluginWrapper pluginWrapper = getPlugin(pluginId);
    	PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
    	if (PluginState.STOPPED == pluginState) {
    		return PluginState.STOPPED;
    	}

        // test for disabled plugin
        if (PluginState.DISABLED == pluginState) {
            // do nothing
            return pluginState;
        }

        if (stopDependents) {
            List<String> dependents = dependencyResolver.getDependents(pluginId);
            while (!dependents.isEmpty()) {
                String dependent = dependents.remove(0);
                stopPlugin(dependent, false);
                dependents.addAll(0, dependencyResolver.getDependents(dependent));
            }
        }

    	try {
    		System.out.println("Stop plugin " + pluginDescriptor.getPluginId() + " " + pluginDescriptor.getVersion());
    		pluginWrapper.getPlugin().stop();
    		pluginWrapper.setPluginState(PluginState.STOPPED);
    		startedPlugins.remove(pluginWrapper);

            firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
    	} catch (PluginException e) {
    		System.err.println(e.getMessage());
    		e.printStackTrace();
    	}

    	return pluginWrapper.getPluginState();
    }

    /**
     * Load plugins.
     */
    @Override
    public void loadPlugins() {
    	// check for plugins directory
        if (!pluginsDirectory.exists() || !pluginsDirectory.isDirectory()) {
            System.err.println("No " + pluginsDirectory.getAbsolutePath() + " directory");
            return;
        }

        // expand all plugin archives
        List<File> pluginArchives = pluginRepository.getPluginArchives();
        for (File archiveFile : pluginArchives) {
            try {
                expandPluginArchive(archiveFile);
            } catch (IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }

        // check for no plugins
        AndFileFilter pluginsFilter = new AndFileFilter(new DirectoryFileFilter());
        pluginsFilter.addFileFilter(new NotFileFilter(createHiddenPluginFilter()));
        File[] directories = pluginsDirectory.listFiles(pluginsFilter);
        if (directories == null) {
        	directories = new File[0];
        }
        if (directories.length == 0) {
        	System.out.println("No plugins");
        	return;
        }

        // load any plugin from plugins directory
       	for (File directory : directories) {
       		try {
       			loadPluginDirectory(directory);
    		} catch (PluginException e) {
    			System.err.println(e.getMessage());
    			e.printStackTrace();
    		}
       	}

        // resolve 'unresolvedPlugins'
        try {
			resolvePlugins();
		} catch (PluginException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
    }

    @Override
    public boolean unloadPlugin(String pluginId) {
    	try {
    		PluginState pluginState = stopPlugin(pluginId);
    		if (PluginState.STARTED == pluginState) {
    			return false;
    		}

    		PluginWrapper pluginWrapper = getPlugin(pluginId);
    		PluginDescriptor descriptor = pluginWrapper.getDescriptor();
    		List<PluginDependency> dependencies = descriptor.getDependencies();
    		for (PluginDependency dependency : dependencies) {
    			if (!unloadPlugin(dependency.getPluginId())) {
    				return false;
    			}
    		}

    		// remove the plugin
    		plugins.remove(pluginId);
    		resolvedPlugins.remove(pluginWrapper);
    		pathToIdMap.remove(pluginWrapper.getPluginPath());

            firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));

    		// remove the classloader
    		if (pluginClassLoaders.containsKey(pluginId)) {
    			PluginClassLoader classLoader = pluginClassLoaders.remove(pluginId);
                classLoader.dispose();
    		}

    		return true;
    	} catch (IllegalArgumentException e) {
    		// ignore not found exceptions because this method is recursive
    	}

    	return false;
    }

    @Override
    public boolean disablePlugin(String pluginId) {
        if (!plugins.containsKey(pluginId)) {
            throw new IllegalArgumentException(String.format("Unknown pluginId %s", pluginId));
        }

        PluginWrapper pluginWrapper = getPlugin(pluginId);
        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
        if (PluginState.DISABLED == pluginState) {
           	return true;
        }

        if (PluginState.STOPPED == stopPlugin(pluginId)) {
            pluginWrapper.setPluginState(PluginState.DISABLED);

            firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, PluginState.STOPPED));

            if (!pluginStatusProvider.disablePlugin(pluginId)) {
                return false;
            }

            System.out.println("Disabled plugin " + pluginDescriptor.getPluginId() + " " + pluginDescriptor.getVersion());

            return true;
        }

        return false;
    }

    @Override
    public boolean enablePlugin(String pluginId) {
        if (!plugins.containsKey(pluginId)) {
            throw new IllegalArgumentException(String.format("Unknown pluginId %s", pluginId));
        }

        PluginWrapper pluginWrapper = getPlugin(pluginId);
       	if (!isPluginValid(pluginWrapper)) {
        	System.out.println("Plugin " + pluginWrapper.getPluginId() + " " + pluginWrapper.getDescriptor().getVersion() + " can not be enabled");
            return false;
        }

        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
        if (PluginState.DISABLED != pluginState) {
            return true;
        }

        if (!pluginStatusProvider.enablePlugin(pluginId)) {
            return false;
        }

        pluginWrapper.setPluginState(PluginState.CREATED);

        firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));

        System.out.println("Enabled plugin " + pluginDescriptor.getPluginId() + " " + pluginDescriptor.getVersion());

        return true;
    }

	public String getpluginsDirectory() {
		return this.pluginsDirectory.getAbsolutePath();
	}
    
    @Override
	public boolean deletePlugin(String pluginId) {
    	if (!plugins.containsKey(pluginId)) {
    		throw new IllegalArgumentException(String.format("Unknown pluginId %s", pluginId));
    	}

		PluginWrapper pluginWrapper = getPlugin(pluginId);
		PluginState pluginState = stopPlugin(pluginId);
		if (PluginState.STARTED == pluginState) {
			System.err.println("Failed to stop plugin " + pluginId + " on delete");
			return false;
		}

		if (!unloadPlugin(pluginId)) {
			System.err.println("Failed to unload plugin " + pluginId + " on delete");
			return false;
		}

		File pluginFolder = new File(pluginsDirectory, pluginWrapper.getPluginPath());

		if (pluginFolder.exists()) {
			FileUtils.delete(pluginFolder);
		}

        pluginRepository.deletePluginArchive(pluginWrapper.getPluginPath());

		return true;
	}

    /**
     * Get plugin class loader for this path.
     */
    @Override
    public PluginClassLoader getPluginClassLoader(String pluginId) {
    	return pluginClassLoaders.get(pluginId);
    }

    @Override
	public RuntimeMode getRuntimeMode() {
    	if (runtimeMode == null) {
        	// retrieves the runtime mode from system
        	String modeAsString = System.getProperty("pf4j.mode", RuntimeMode.DEPLOYMENT.toString());
        	runtimeMode = RuntimeMode.byName(modeAsString);
    	}

		return runtimeMode;
	}

    @Override
    public PluginWrapper whichPlugin(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        for (PluginWrapper plugin : resolvedPlugins) {
            if (plugin.getPluginClassLoader() == classLoader) {
            	return plugin;
            }
        }

        return null;
    }

    @Override
    public synchronized void addPluginStateListener(PluginStateListener listener) {
        pluginStateListeners.add(listener);
    }

    @Override
    public synchronized void removePluginStateListener(PluginStateListener listener) {
        pluginStateListeners.remove(listener);
    }

    public Version getVersion() {
        String version = null;

        Package pf4jPackage = getClass().getPackage();
        if (pf4jPackage != null) {
            version = pf4jPackage.getImplementationVersion();
            if (version == null) {
                version = pf4jPackage.getSpecificationVersion();
            }
        }

        return (version != null) ? Version.valueOf(version) : Version.forIntegers(0, 0, 0);
    }

    /**
	 * Add the possibility to override the PluginDescriptorFinder.
	 * By default if getRuntimeMode() returns RuntimeMode.DEVELOPMENT than a
	 * PropertiesPluginDescriptorFinder is returned else this method returns
	 * DefaultPluginDescriptorFinder.
	 */
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
    		return new PropertiesPluginDescriptorFinder();
    		/*
        	if (RuntimeMode.DEVELOPMENT.equals(getRuntimeMode())) {
    	}

    	return new DefaultPluginDescriptorFinder(pluginClasspath);
    	*/
    }

    /**
     * Add the possibility to override the PluginClassPath.
     * By default if getRuntimeMode() returns RuntimeMode.DEVELOPMENT than a
	 * DevelopmentPluginClasspath is returned else this method returns
	 * PluginClasspath.
     */
    protected PluginClasspath createPluginClasspath() {
    	if (RuntimeMode.DEVELOPMENT.equals(getRuntimeMode())) {
    		return new DevelopmentPluginClasspath();
    	}

    	return new PluginClasspath();
    }

    protected PluginStatusProvider createPluginStatusProvider() {
        return new DefaultPluginStatusProvider(pluginsDirectory);
    }

    protected PluginRepository createPluginRepository() {
        return new DefaultPluginRepository(pluginsDirectory, new ZipFileFilter());
    }

    protected boolean isPluginDisabled(String pluginId) {
    	return pluginStatusProvider.isPluginDisabled(pluginId);
    }

    protected boolean isPluginValid(PluginWrapper pluginWrapper) {
    	Expression requires = pluginWrapper.getDescriptor().getRequires();
    	Version system = getSystemVersion();
    	if (requires.interpret(system)) {
    		return true;
    	}

    	System.out.println("Plugin " + pluginWrapper.getPluginId() + " " + pluginWrapper.getDescriptor().getVersion() + " requires a minimum system version of " + requires);

    	return false;
    }

    protected FileFilter createHiddenPluginFilter() {
    	OrFileFilter hiddenPluginFilter = new OrFileFilter(new HiddenFilter());

        if (RuntimeMode.DEVELOPMENT.equals(getRuntimeMode())) {
            hiddenPluginFilter.addFileFilter(new NameFileFilter("target"));
        }

        return hiddenPluginFilter;
    }

    /**
     * Add the possibility to override the plugins directory.
     * If a "pf4j.pluginsDir" system property is defined than this method returns
     * that directory.
     * If getRuntimeMode() returns RuntimeMode.DEVELOPMENT than a
	 * DEVELOPMENT_PLUGINS_DIRECTORY ("../plugins") is returned else this method returns
	 * DEFAULT_PLUGINS_DIRECTORY ("plugins").
     * @return
     */
    protected File createPluginsDirectory() {
    	String pluginsDir = System.getProperty("pf4j.pluginsDir");
    	if (pluginsDir == null) {
    		if (RuntimeMode.DEVELOPMENT.equals(getRuntimeMode())) {
    			pluginsDir = DEVELOPMENT_PLUGINS_DIRECTORY;
    		} else {
    			pluginsDir = DEFAULT_PLUGINS_DIRECTORY;
    		}
    	}

    	return new File(pluginsDir);
    }

    /**
     * Add the possibility to override the PluginFactory..
     */
    protected PluginFactory createPluginFactory() {
        return new DefaultPluginFactory();
    }

    /**
     * Add the possibility to override the PluginClassLoader.
     */
    protected PluginClassLoader createPluginClassLoader(PluginDescriptor pluginDescriptor) {
        return new PluginClassLoader(this, pluginDescriptor, getClass().getClassLoader());
    }

    private void initialize() {
		plugins = new HashMap<>();
        pluginClassLoaders = new HashMap<>();
        pathToIdMap = new HashMap<>();
        unresolvedPlugins = new ArrayList<>();
        resolvedPlugins = new ArrayList<>();
        startedPlugins = new ArrayList<>();

        pluginStateListeners = new ArrayList<>();

    	new EventManager();
        dependencyResolver = new DependencyResolver();

        pluginClasspath = createPluginClasspath();
        pluginFactory = createPluginFactory();
        pluginDescriptorFinder = createPluginDescriptorFinder();
        pluginStatusProvider = createPluginStatusProvider();
        pluginRepository = createPluginRepository();

        try {
            pluginsDirectory = pluginsDirectory.getCanonicalFile();
        } catch (IOException e) {
        	System.out.println(e.getMessage());
        }
        System.setProperty("pf4j.pluginsDir", pluginsDirectory.getAbsolutePath());
	}

    private PluginWrapper loadPluginDirectory(File pluginDirectory) throws PluginException {
        // try to load the plugin
		String pluginName = pluginDirectory.getName();
        String pluginPath = "/".concat(pluginName);

        // test for plugin duplication
        if (plugins.get(pathToIdMap.get(pluginPath)) != null) {
            return null;
        }

        // retrieves the plugin descriptor
        PluginDescriptor pluginDescriptor = pluginDescriptorFinder.find(pluginDirectory);

        // load plugin
        PluginClassLoader pluginClassLoader = createPluginClassLoader(pluginDescriptor);
        PluginLoader pluginLoader = new PluginLoader(pluginDirectory, pluginClassLoader, pluginClasspath);
        pluginLoader.load();

        // create the plugin wrapper
        PluginWrapper pluginWrapper = new PluginWrapper(this, pluginDescriptor, pluginPath, pluginClassLoader);
        pluginWrapper.setPluginFactory(pluginFactory);
        pluginWrapper.setRuntimeMode(getRuntimeMode());

        // test for disabled plugin
        if (isPluginDisabled(pluginDescriptor.getPluginId())) {
            System.out.println("Plugin " + pluginPath + " is disabled");
            pluginWrapper.setPluginState(PluginState.DISABLED);
        }

        // validate the plugin
        if (!isPluginValid(pluginWrapper)) {
        	System.out.println("Plugin " + pluginPath + " is disabled");
        	pluginWrapper.setPluginState(PluginState.DISABLED);
        }
        
        String pluginId = pluginDescriptor.getPluginId();

        // add plugin to the list with plugins
        plugins.put(pluginId, pluginWrapper);
        unresolvedPlugins.add(pluginWrapper);

        // add plugin class loader to the list with class loaders
        pluginClassLoaders.put(pluginId, pluginClassLoader);

        return pluginWrapper;
    }

    private File expandPluginArchive(File pluginArchiveFile) throws IOException {
    	String fileName = pluginArchiveFile.getName();
        long pluginArchiveDate = pluginArchiveFile.lastModified();
        String pluginName = fileName.substring(0, fileName.length() - 4);
        File pluginDirectory = new File(pluginsDirectory, pluginName);
        // check if exists directory or the '.zip' file is "newer" than directory
        if (!pluginDirectory.exists() || (pluginArchiveDate > pluginDirectory.lastModified())) {

        	// do not overwrite an old version, remove it
        	if (pluginDirectory.exists()) {
        		FileUtils.delete(pluginDirectory);
        	}

            // create directory for plugin
            pluginDirectory.mkdirs();

            // expand '.zip' file
            Unzip unzip = new Unzip();
            unzip.setSource(pluginArchiveFile);
            unzip.setDestination(pluginDirectory);
            unzip.extract();
        }

        return pluginDirectory;
    }

	private void resolvePlugins() throws PluginException {
		resolveDependencies();
	}

	private void resolveDependencies() throws PluginException {
		dependencyResolver.resolve(unresolvedPlugins);
		resolvedPlugins = dependencyResolver.getSortedPlugins();
        for (PluginWrapper pluginWrapper : resolvedPlugins) {
        	unresolvedPlugins.remove(pluginWrapper);
        	System.out.println("Plugin " + pluginWrapper.getDescriptor().getPluginId() + " resolved");
        }
	}

    private synchronized void firePluginStateEvent(PluginStateEvent event) {
        for (PluginStateListener listener : pluginStateListeners) {
            listener.pluginStateChanged(event);
        }
    }

	@Override
	public synchronized EventManager getEventManager() {
		if (EventManager.instance == null) {
			new EventManager();
		}
		return EventManager.instance;
	}


}
