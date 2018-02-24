// IThermalPrinter.aidl
package com.justtide.aidl;

// Declare any non-default types here with import statements

interface IThermalPrinter {

    int open();

    int close();

    int print(in byte[] writeByte);

    //int printLegency(Bitmap bitmap);

    //int print(Bitmap bitmap);

    void cancel();

    boolean isFinished();

    int waitForPrintFinish(int timeoutMs);

    int getExpValue();
}
