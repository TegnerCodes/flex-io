package com.tegnercodes.flexio.pluginsystem.util;

/**
 * File filter that accepts all files ending with .JAR.
 * This filter is case insensitive.
 */
public class JarFileFilter extends ExtensionFileFilter {

    /**
     * The extension that this filter will search for.
     */
    private static final String JAR_EXTENSION = ".JAR";

    public JarFileFilter() {
        super(JAR_EXTENSION);
    }

}
