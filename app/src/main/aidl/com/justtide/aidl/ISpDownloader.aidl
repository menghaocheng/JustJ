// ISpDownloader.aidl
package com.justtide.aidl;

// Declare any non-default types here with import statements

interface ISpDownloader {

    int open();

    int close();

    int down(in byte[] writeByte);

    int waitForUpdateFinish(int timeoutMs);

    //*int waitForUpdateFinish();

    String getVersion();

    int getExpValue();
}
