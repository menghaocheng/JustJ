package com.justtide.aidl;

interface ISpSysCtrl {

     String getVersion();

    int reboot();

    int beep(int frequncy, int timeMs);

    //int setTime(long timestamp);

    //long getTime();

    int ledControl(int ledName, int ledMode);

    byte[] getSn();

    int setSn(in byte[] sn);


    int spLogOpen();

    int spLogClose();

    int exeRootCmd(String cmdStr);

    int exeCmd(String cmdStr);


    //int setTime(Time time);

    //Time getTime();

}
