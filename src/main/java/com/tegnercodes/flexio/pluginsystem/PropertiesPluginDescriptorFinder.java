package com.tegnercodes.flexio.pluginsystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.github.zafarkhaja.semver.Version;
import com.tegnercodes.flexio.pluginsystem.util.StringUtils;

/**
 * Find a plugin descriptor in a properties file (in plugin repository).
 */
public class PropertiesPluginDescriptorFinder implements PluginDescriptorFinder {

	private static final String DEFAULT_PROPERTIES_FILE_NAME = "plugin.properties";

	private String propertiesFileName;

	public PropertiesPluginDescriptorFinder() {
		this(DEFAULT_PROPERTIES_FILE_NAME);
	}

	public PropertiesPluginDescriptorFinder(String propertiesFileName) {
		this.propertiesFileName = propertiesFileName;
	}

	@Override
	public PluginDescriptor find(File pluginRepository) throws PluginException {
        Properties properties = readProperties(pluginRepository);

        PluginDescriptor pluginDescriptor = createPluginDescriptor(properties);
        validatePluginDescriptor(pluginDescriptor);

        return pluginDescriptor;
	}

    protected Properties readProperties(File pluginRepository) throws PluginException {
        File propertiesFile = new File(pluginRepository, propertiesFileName);
        if (!propertiesFile.exists()) {
            throw new PluginException("Cannot find '" + propertiesFile + "' file");
        }

        InputStream input = null;
        try {
            input = new FileInputStream(propertiesFile);
        } catch (FileNotFoundException e) {
            // not happening
        }

        Properties properties = new Properties();
        try {
            properties.load(input);
        } catch (IOException e) {
            throw new PluginException(e.getMessage(), e);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                throw new PluginException(e.getMessage(), e);
            }
        }

        return properties;
    }

    protected PluginDescriptor createPluginDescriptor(Properties properties) {
        PluginDescriptor pluginDescriptor = new PluginDescriptor();

        // TODO validate !!!
        String id = properties.getProperty("plugin.id");
        pluginDescriptor.setPluginId(id);

        String clazz = properties.getProperty("plugin.class");
        pluginDescriptor.setPluginClass(clazz);

        String version = properties.getProperty("plugin.version");
        if (StringUtils.isNotEmpty(version)) {
            pluginDescriptor.setPluginVersion(Version.valueOf(version));
        }

        String provider = properties.getProperty("plugin.provider");
        pluginDescriptor.setProvider(provider);

        String dependencies = properties.getProperty("plugin.dependencies");
        pluginDescriptor.setDependencies(dependencies);

        return pluginDescriptor;
    }

    protected void validatePluginDescriptor(PluginDescriptor pluginDescriptor) throws PluginException {
        if (StringUtils.isEmpty(pluginDescriptor.getPluginId())) {
            throw new PluginException("plugin.id cannot be empty");
        }
        if (StringUtils.isEmpty(pluginDescriptor.getPluginClass())) {
            throw new PluginException("plugin.class cannot be empty");
        }
        if (pluginDescriptor.getVersion() == null) {
            throw new PluginException("plugin.version cannot be empty");
        }
        if (StringUtils.isEmpty(pluginDescriptor.getProvider()) || pluginDescriptor.getProvider() == null) {
            throw new PluginException("Plugin.provider cannot be empty");
        }
    }

}
