// IPsamReader.aidl
package com.justtide.aidl;

import com.justtide.aidl.ContactCard;
import com.justtide.aidl.CommandApdu;
import com.justtide.aidl.ResponseApdu;


interface IPsamReader {

    int open(byte slot, boolean emvMode);

    //int open(byte slot);

    int close();

    int checkStop();

    ContactCard enable(byte slot, byte vccMode, boolean emvMode);

    //ContactCard enable(byte slot, byte vccMode);

    //ContactCard enable(byte slot);

    //ContactCard enable();

    int disable();

    ResponseApdu transmit(in ContactCard contactCard, in CommandApdu command);

}
