package com.aimbrain.sdk;

import com.aimbrain.sdk.array.Arrays;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


public class ArraysTest {

    @Test
    public void testContains() {
        int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        assertTrue(Arrays.contains(array, 1));
        assertTrue(Arrays.contains(array, 5));
        assertFalse(Arrays.contains(array, 10));
    }
}
