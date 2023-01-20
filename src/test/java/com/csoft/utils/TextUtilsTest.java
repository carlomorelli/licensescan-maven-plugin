package com.csoft.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TextUtilsTest {

    @Test
    public void testParseAsRegex_WHEN_inputContainsXMLEscapedChars_THEN_returnsStringWithUnescapedChars() {
        String input = "regex:.*(?&lt;!\\+\\s?)GNU General Public License.*";
        String parsed = TextUtils.parseAsRegex(input);
        assertEquals(".*(?<!\\+\\s?)GNU General Public License.*", parsed);
    }

    @Test
    public void testParseAsRegex_WHEN_inputContainsNormalChars_THEN_returnsSameString() {
        String input = "this is a normal string";
        String parsed = TextUtils.parseAsRegex(input);
        assertEquals(input, parsed);
    }

    @Test
    public void testParseAsRegex_WHEN_inputContainsNormalCharsWithRegexPRefix_THEN_returnsSubstring() {
        String input = "regex:\\this is a \n ormal string";
        String parsed = TextUtils.parseAsRegex(input);
        assertEquals("\\this is a \n ormal string", parsed);
    }

}
