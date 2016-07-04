package com.tegnercodes.flexio.pluginsystem.util;

public class StringUtils {

	public static boolean isEmpty(String str) {
		return (str == null) || str.isEmpty();
	}

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

}
