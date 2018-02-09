// IGuomi.aidl
package com.justtide.aidl;

// Declare any non-default types here with import statements

interface IGuomiReader {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
   // void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
   //         double aDouble, String aString);
    String getVersion();

    int open();

    int close();

    //ResponseApdu transmit(CommandApdu command, int timeoutMs);
}
