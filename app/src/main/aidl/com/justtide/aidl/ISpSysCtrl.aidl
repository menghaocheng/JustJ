package com.justtide.aidl;

import com.justtide.aidl.Time;

interface ISpSysCtrl {

     String getVersion();

    int reboot();

    int beep(int frequncy, int timeMs);

    int ledControl(int ledName, int ledMode);

    byte[] getSn();

    int setSn(in byte[] sn);

    int spLogOpen();

    int spLogClose();

    int exeRootCmd(String cmdStr);

    int exeCmd(String cmdStr);

    int setTime(in Time time);

    Time getTime();

    int getExpValue();
}
