package com.csoft.utils;

import org.apache.commons.text.StringEscapeUtils;

public class TextUtils {

    public static String parseAsRegex(final String text) {
        String regex = text.replace("regex:", "");
        return StringEscapeUtils.unescapeXml(regex);
    }

}
