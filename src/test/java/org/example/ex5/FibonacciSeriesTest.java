package org.example.ex5;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

public class FibonacciSeriesTest {

    @Test
    public void testFibonacciBaseCases() {
        assertEquals(0, FibonacciSeries.fibonacci(0));
        assertEquals(1, FibonacciSeries.fibonacci(1));
    }

    @Test
    public void testFibonacciPositiveNumbers() {
        assertEquals(1, FibonacciSeries.fibonacci(2));
        assertEquals(2, FibonacciSeries.fibonacci(3));
        assertEquals(3, FibonacciSeries.fibonacci(4));
        assertEquals(5, FibonacciSeries.fibonacci(5));
        assertEquals(8, FibonacciSeries.fibonacci(6));
        assertEquals(13, FibonacciSeries.fibonacci(7));
        assertEquals(21, FibonacciSeries.fibonacci(8));
        assertEquals(34, FibonacciSeries.fibonacci(9));
        assertEquals(55, FibonacciSeries.fibonacci(10));
    }

    @Test
    public void testFibonacciEdgeCase() {
        assertEquals(1, FibonacciSeries.fibonacci(2));
    }

    @Test
    public void testFibonacciLargePositiveNumber() {
        assertEquals(377, FibonacciSeries.fibonacci(14));
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        Constructor<FibonacciSeries> constructor = FibonacciSeries.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }
}

