// IJustjService.aidl
package com.justtide.aidl;

// Declare any non-default types here with import statements

interface AidlJustjService {
    //Ibinder getSystemService();
    //Ibinder getMagCardReader();
    String hello(String inStr);

    IBinder getSpSysCtrl();

    IBinder getIccReader();

    IBinder getPsamReader();

    IBinder getMagcardReader();

    IBinder getPiccReader();

    IBinder getThermalPrinter();

    IBinder getPinPad(int devid);

    IBinder getDownload();



}
