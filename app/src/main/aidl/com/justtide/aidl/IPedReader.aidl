// PedReader.aidl
package com.justtide.aidl;

import com.justtide.aidl.HsmObj;
import com.justtide.aidl.Task;
import com.justtide.aidl.RsaPinKey;
import com.justtide.aidl.PedConfig;

// Declare any non-default types here with import statements

interface IPedReader {

    String hello(String inStr);

    void setExpValue(int errCode);

    int getExpValue();

    PedConfig getConfig();

    byte[] getRandom(int randomLen);

    int deleteKey(int keyType, int keyIndex);

    int getSensitiveTime(int sensitiveType);

    int setPinInputTimeout(int timeoutSc);

    int writePinKey(int mode, int mastKeyId, int destKeyId, in byte[] keyData);

    int writeMacKey(int mode, int mastKeyId, int destKeyId,in byte[] keyData);

    int writeMasterKey(int mode, int mastKeyId, int destKeyId, in byte[] keyData);

   // byte[] getPin(int keyIndex, int pinMode, in byte[] cardNo, in byte[] expectPinLenList, Context cont);

   // byte[] getPin(int keyIndex, int pinMode, in byte[] cardNo, in byte[] expectPinLenList, Context cont, String priceStr);

   byte[] getPin(int keyIndex, int pinMode, in byte[] cardNo, in byte[] expectPinLenList, String priceStr,String title);


    int getPinCancel();

    boolean isGetPinIng();

   // byte[] iccOfflinePlainPin(byte[] expectPinLenList, byte[] apduData, Context cont);

    //byte[] iccOfflinePlainPin(byte[] expectPinLenList, byte[] apduData, Context cont, String pricStr);

   // byte[] iccOfflinePlainPin(byte[] expectPinLenList, byte[] apduData, Context cont, String pricStr, String title);
    byte[] iccOfflinePlainPin(in byte[] expectPinLenList, in byte[] apduData, String pricStr, String title);

   // byte[] iccOfflineCipherPin(byte[] expectPinLenList, byte[] apduData, RsaPinKey rsaPinKey, Context cont);

  //  byte[] iccOfflineCipherPin(byte[] expectPinLenList, byte[] apduData, RsaPinKey rsaPinKey, Context cont, String pricStr);

   // byte[] iccOfflineCipherPin(byte[] expectPinLenList, byte[] apduData, RsaPinKey rsaPinKey, Context cont, String pricStr, String title);
   byte[] iccOfflineCipherPin(in byte[] expectPinLenList, in byte[] apduData, in RsaPinKey rsaPinKey, String pricStr, String title);

    byte[] getMac(int keyIndex, int macMode, in byte[] inMacData);

    int writeDataKey(int mode, int dataKeyId, int desKeyId,in byte[] keyData);

    byte[] dataEncrypt(int keyIndex, int dataMode, in byte[] inData);

    int snKey(in byte[] snKey);

    int selfKeyCheck();

    byte[] snEncrypt(in byte[] sn);

    int hsmSave(in HsmObj hsmObj, in byte[] objectData, int nDataType);

    int hsmDelete(in HsmObj hsmObj, in byte[] pPIN);

    int hsmDeleteAll(in byte[] pPIN);

    int hsmQueryCount(in HsmObj hsmObj);

    byte[] hsmQueryName(int objectType, int dataType);

    byte[] hsmLoad(int nIndex, in HsmObj hsmObj, int nDataType);

    int hsmGetFreeSpace();

    int injectDesKey(int desKeyId, in byte[] desKey);

    byte[] calcWkeyKcv(int keyType, int keyIndex);

    byte[] tdea(int mode, int keyType, int keyIndex, in byte[] IV, in byte[] inBuff);

    int getKeyLen(int keyType, int keyIndex);

    int deleteKeys(String pkgName);

    int isKeyExist(String pkgName);

    int writeAesPinKey(int mode, int mastKeyId, int desKeyId,in byte[] keyData);

    byte[] calcCmacKey(in byte[] mastKey, in byte[] inKey);

    int savePassword(int index, in byte[] password);

    int checkPassword(int index, in byte[] password);

    byte[] tdeaDencrypt(int mode, in byte[] password, in byte[] data);

    int syncTime();

    int importPin(byte endKey, in byte[] inPinData);

    int fsFormat(int timeoutMs);

    //*int fsFormat();

    int pedFormat(int timeoutMs);

    //*int pedFormat();

    int secMaintainCheck();

    int secMaintainActivate(in byte[] actPara);

    //*int secMaintainActivate();

    Task taskWait(int timeoutMs);
}
