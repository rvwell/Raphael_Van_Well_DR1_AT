package org.example.ex5;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class GenerateSubsetsTest {

    @Test
    void subsetRecursionTestOne() {
        String str = "abc";
        String[] expected = new String[] {"abc", "ab", "ac", "a", "bc", "b", "c", ""};

        List<String> ans = GenerateSubsets.subsetRecursion(str);
        assertArrayEquals(ans.toArray(), expected);
    }

    @Test
    void subsetRecursionTestTwo() {
        String str = "cbf";
        String[] expected = new String[] {"cbf", "cb", "cf", "c", "bf", "b", "f", ""};

        List<String> ans = GenerateSubsets.subsetRecursion(str);
        assertArrayEquals(ans.toArray(), expected);
    }

    @Test
    void subsetRecursionTestThree() {
        String str = "aba";
        String[] expected = new String[] {"aba", "ab", "aa", "a", "ba", "b", "a", ""};

        List<String> ans = GenerateSubsets.subsetRecursion(str);
        assertArrayEquals(ans.toArray(), expected);
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        Constructor<GenerateSubsets> constructor = GenerateSubsets.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }
}