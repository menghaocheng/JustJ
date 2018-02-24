// IMagReader.aidl
package com.justtide.aidl;

import com.justtide.aidl.MagneticCard;

interface IMagcardReader {

    int open();

    int close();

    int reset();

    boolean detect();

    MagneticCard read();

    int setCheckLrc(boolean value);

    byte[] getCardNuber();

    int getExpValue();
}
