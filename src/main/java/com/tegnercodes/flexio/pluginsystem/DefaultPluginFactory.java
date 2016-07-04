package com.tegnercodes.flexio.pluginsystem;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * The default implementation for PluginFactory.
 * It uses Class.newInstance() method.
 */
public class DefaultPluginFactory implements PluginFactory {

    /**
     * Creates a plugin instance. If an error occurs than that error is logged and the method returns null.
     * @param pluginWrapper
     * @return
     */
    @Override
    public Plugin create(final PluginWrapper pluginWrapper) {
        String pluginClassName = pluginWrapper.getDescriptor().getPluginClass();

        Class<?> pluginClass;
        try {
            pluginClass = pluginWrapper.getPluginClassLoader().loadClass(pluginClassName);
        } catch (ClassNotFoundException e) {
        	System.err.println(e.getMessage());
        	e.printStackTrace();
            return null;
        }

        // once we have the class, we can do some checks on it to ensure
        // that it is a valid implementation of a plugin.
        int modifiers = pluginClass.getModifiers();
        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)
                || (!Plugin.class.isAssignableFrom(pluginClass))) {
            System.err.println("The plugin class " + pluginClassName + " is not valid");
            return null;
        }

        // create the plugin instance
        try {
            Constructor<?> constructor = pluginClass.getConstructor(PluginWrapper.class);
            return (Plugin) constructor.newInstance(pluginWrapper);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

}
