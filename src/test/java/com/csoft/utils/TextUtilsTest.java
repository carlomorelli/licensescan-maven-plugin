package com.csoft.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TextUtilsTest {

    @Test
    public void testParseAsRegex() {
        String input = "regex:.*(?&lt;!\\+\\s?)GNU General Public License.*";
        String parsed = TextUtils.parseAsRegex(input);
        assertEquals(".*(?<!\\+\\s?)GNU General Public License.*", parsed);
    }

}
