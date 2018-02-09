// IThermalPrinter.aidl
package com.justtide.aidl;

// Declare any non-default types here with import statements

interface IThermalPrinter {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
     //       double aDouble, String aString);

    int open();

    int close();

    int print(in byte[] writeByte);

    //int printLegency(Bitmap bitmap);

    //int print(Bitmap bitmap);

    void cancel();

    boolean isFinished();

    int waitForPrintFinish(int timeoutMs);
}
