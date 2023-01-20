package com.csoft.utils;

import org.apache.commons.text.StringEscapeUtils;

public class TextUtils {

    /**
     * Parses a regex, removes the prefix :regex if found, and returns the
     * XML-unescaped version.
     * 
     * @param text Input string containing a regex.
     * @return Unescaped string with prefix :regex removed.
     */
    public static String parseAsRegex(final String text) {
        String regex = text.replace("regex:", "");
        return StringEscapeUtils.unescapeXml(regex);
    }

}
