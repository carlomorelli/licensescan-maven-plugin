package com.csoft.utils;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TextUtilsTest {

    @Test
    public void testParseAsRegex_WHEN_inputContainsXMLEscapedChars_THEN_returnsStringWithUnescapedChars() {
        String input = "regex:.*(?&lt;!\\+\\s?)GNU General Public License.*";
        String parsed = TextUtils.parseAsRegex(input);
        assertThat(parsed, is(".*(?<!\\+\\s?)GNU General Public License.*"));
    }

    @Test
    public void testParseAsRegex_WHEN_inputContainsNormalChars_THEN_returnsSameString() {
        String input = "this is a normal string";
        String parsed = TextUtils.parseAsRegex(input);
        assertThat(parsed, is(input));
    }

    @Test
    public void testParseAsRegex_WHEN_inputContainsNormalCharsWithRegexPRefix_THEN_returnsSubstring() {
        String input = "regex:\\this is a \n ormal string";
        String parsed = TextUtils.parseAsRegex(input);
        assertThat(parsed, is("\\this is a \n ormal string"));
    }

}
