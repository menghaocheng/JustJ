// IPiccReader.aidl
package com.justtide.aidl;

// Declare any non-default types here with import statements

interface IPiccReader {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    /*void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);*/

     int open(byte slot);

     //int open();

     int close();

     int search(byte pollMode, byte cardType, int timeoutMs);

     /*int search(byte cardType, int timeouMs);

     int search(int timeouMs);

     int search();*/

    //int detect(PiccInterface piccInterface);

     int checkCardType(int cardType);

     void searchStop();

     //ResponseApdu transmit(CommandApdu command, in byte[] exPara);

     int remove();

     boolean checkIfRemoved();

     int m1Authentication(byte blockNumber, byte keyType, in byte[] key);

     byte[] m1BlockDataRead(byte blockNumber);

     int m1BlockDataWrite(byte blockNumber, in byte[] inData);

     int m1ValueDataSet(byte blockNumber, int value);

     int m1ValueDataRead(byte blockNumber, in int[] outValue);

     int m1ValueDataAdd(byte blockNumber, int value);

     int m1ValueDataDel(byte blockNumber, int value);

     int m1ValueDataSaveOpRet(byte blockNumber);

     int m1ValueDataUnloading(byte blockNumber);

     byte[] getConfig();

     int setConfig(byte printConf, in byte[] abCardValidRegValue);
}
