// IJustjService.aidl
package com.justtide.aidl;

// Declare any non-default types here with import statements

interface AidlJustjService {
    //Ibinder getSystemService();
    //Ibinder getMagCardReader();
    String hello(String inStr);

    IBinder getPinPad(int devid);

    IBinder getSpSysCtrl();

}
