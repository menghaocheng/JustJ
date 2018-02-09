// IMagReader.aidl
package com.justtide.aidl;

// Declare any non-default types here with import statements

interface IMagcardReader {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
    //        double aDouble, String aString);

    int open();

    int close();

    //int reset();

    boolean detect();

    //MagneticCard read();

    int setCheckLrc(boolean value);

    byte[] getCardNuber();


}
