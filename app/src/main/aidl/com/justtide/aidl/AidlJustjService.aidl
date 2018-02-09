// IJustjService.aidl
package com.justtide.aidl;

// Declare any non-default types here with import statements

interface AidlJustjService {
    //Ibinder getSystemService();
    //Ibinder getMagCardReader();
    String hello(String inStr);

    IBinder getSpSysCtrl();

    IBinder getIccReader();

    IBinder getPiccReader();

    IBinder getPinPad(int devid);

}
