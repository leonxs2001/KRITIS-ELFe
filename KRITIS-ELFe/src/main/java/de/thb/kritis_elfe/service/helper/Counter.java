package de.thb.kritis_elfe.service.helper;

/**
 * Is a helper for counting up an Integer-Value from 0
 * Used in the templates to count up
 */
public class Counter {
    private int value = 0;

    public int countAndGet(){
        return value++;
    }

    public int getValue() {
        return value;
    }
}
