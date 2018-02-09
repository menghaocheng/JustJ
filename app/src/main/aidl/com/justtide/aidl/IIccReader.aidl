// IIccReader.aidl
package com.justtide.aidl;

// Declare any non-default types here with import statements

interface IIccReader {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    /*void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
            */
    int open(byte slot, boolean emvMode);

//    int open(byte slot);

  //  int open();

    int close();

    int check(int timeoutMs);

    //int check();

    int checkStop();

    //ContactCard enable(byte slot, byte vccMode, boolean emvMode);

    //ContactCard enable(byte slot, byte vccMode);

    //ContactCard enable(byte slot);

    //ContactCard enable();

    //int disable();

    //ResponseApdu transmit(ContactCard contactCard, CommandApdu command);


}
