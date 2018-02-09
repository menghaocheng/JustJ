// ISpDownloader.aidl
package com.justtide.aidl;

// Declare any non-default types here with import statements

interface ISpDownloader {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    /*void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);*/

    int open();

    int close();

    int down(in byte[] writeByte);

    int waitForUpdateFinish(int timeoutMs);

    //int waitForUpdateFinish();

    String getVersion();
}
