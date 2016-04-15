package com.aimbrain.sdk.array;


public class Arrays {

    public static boolean contains(int[] array, int value){
        for(int currentValue : array)
        {
            if(currentValue == value)
                return true;
        }
        return false;
    }
}
