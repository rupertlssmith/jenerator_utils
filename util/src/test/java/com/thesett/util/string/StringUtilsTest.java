package com.thesett.util.string;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class StringUtilsTest {
    @Test
    public void camelCaseString() {
        assertEquals("", StringUtils.toCamelCase(""));
        assertEquals("aB", StringUtils.toCamelCase("a_b"));
        assertEquals("abc", StringUtils.toCamelCase("abc"));
        assertEquals("abcDef", StringUtils.toCamelCase("abc_def"));
    }

}
