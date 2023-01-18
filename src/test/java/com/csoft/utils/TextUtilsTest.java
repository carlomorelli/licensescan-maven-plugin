package com.csoft.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TextUtilsTest {

    @Test
    public void testParseAsRegex() {
        String input = "regex:.*(?&lt;!\\+\\s?)GNU General Public License.*";
        String parsed = TextUtils.parseAsRegex(input);
        assertEquals(".*(?<!\\+\\s?)GNU General Public License.*", parsed);
    }

}
