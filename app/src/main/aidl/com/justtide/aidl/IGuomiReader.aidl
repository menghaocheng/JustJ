// IGuomi.aidl
package com.justtide.aidl;

import com.justtide.aidl.CommandApdu;
import com.justtide.aidl.ResponseApdu;

interface IGuomiReader {

    String getVersion();

    int open();

    int close();

    ResponseApdu transmit(in CommandApdu command, int timeoutMs);

    void setExpValue(int errCode);

    int getExpValue();
}
