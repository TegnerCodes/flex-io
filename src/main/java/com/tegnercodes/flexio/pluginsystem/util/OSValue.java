package com.tegnercodes.flexio.pluginsystem.util;

public class OSValue {
	public static String getOSName() 
	{ 
	String osNameProperty = System.getProperty("os.name"); 

	if (osNameProperty == null) 
	{ 
		throw new RuntimeException("os.name property is not set"); 
	} 
	else 
	{ 
	osNameProperty = osNameProperty.toLowerCase(); 
	} 

	if (osNameProperty.contains("win")) 
	{ 
	return "windows"; 
	} 
	else if (osNameProperty.contains("mac")) 
	{ 
	return "mac"; 
	} 
	else if (osNameProperty.contains("linux") || osNameProperty.contains("nix")) 
	{ 
	return "linux"; 
	} 
	else 
	{ 
	throw new RuntimeException("Unknown OS name: " + osNameProperty); 
	} 
	}


}
