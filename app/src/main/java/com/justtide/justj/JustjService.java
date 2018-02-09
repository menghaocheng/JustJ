package com.justtide.justj;

import com.just.api.Device;
import com.just.api.PosDevice;
import com.just.api.UtilFun;
import com.justtide.aidl.AidlJustjService;
import com.justtide.aidl.IIccReader;
import com.justtide.aidl.IMagcardReader;
import com.justtide.aidl.IPedReader;
import com.justtide.aidl.IPiccReader;
import com.justtide.aidl.IPsamReader;
import com.justtide.aidl.ISpDownloader;
import com.justtide.aidl.ISpSysCtrl;
//import com.justtide.aidl.AidlJustjService;


import com.justtide.justtide.IccReader;
import com.justtide.justtide.MagcardReader;
import com.justtide.justtide.PiccReader;
import com.justtide.justtide.PsamReader;
import com.justtide.justtide.SpDownloader;
import com.justtide.justtide.SpSysCtrl;
import com.justtide.justtide.PedReader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

public class JustjService extends Service {

    private static final String TAG = "JustjService";
    private static boolean dbg = false;


    PosDevice PosDevice;

    SpSysCtrl mSpSysCtrl;
    IccReader mIccReader;
    PsamReader mPsamReader;
    PiccReader mPiccReader;
    MagcardReader mMagcardReader;
    PedReader mPedReader;
    SpDownloader mSpDownloader;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();

        PosDevice = new PosDevice(-1);

        mSpSysCtrl = SpSysCtrl.getInstance();
        mIccReader = IccReader.getInstance();
        mPsamReader = PsamReader.getInstance();
        mPiccReader = PiccReader.getInstance();
        mMagcardReader = MagcardReader.getInstance();
        mPedReader = PedReader.getInstance(null);
        mSpDownloader = SpDownloader.getInstance();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: startId = " + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy:");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "service on unbind");
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind:");
        return mJustjService;
    }

    private final AidlJustjService.Stub mJustjService = new AidlJustjService.Stub() {

        @Override
        public String hello(String inStr) throws RemoteException {
            Log.d(TAG, "test: inStr =" + inStr);
            return mIPedReader.hello(inStr);
        }

        @Override
        public IBinder getSpSysCtrl() throws RemoteException {
            return mISpSysCtrl;
        }

        @Override
        public IBinder getIccReader() throws RemoteException {
            return mIIccReader;
        }

        @Override
        public IBinder getPsamReader() throws RemoteException {
            return mIPsamReader;
        }

        @Override
        public IBinder getPiccReader() throws RemoteException {
            return mIPiccReader;
        }

        @Override
        public IBinder getPinPad(int devid) throws RemoteException {
            return mIPedReader;
        }

        @Override
        public IBinder getDownload() throws RemoteException {
            return mISpDownloader;
        }
    };



    private final ISpSysCtrl.Stub mISpSysCtrl = new ISpSysCtrl.Stub(){
        @Override
        public String getVersion() throws RemoteException {
            Log.d(TAG, "getVersion: ");
            return mSpSysCtrl.getVersion();
        }

        @Override
        public int reboot() throws RemoteException {
            return mSpSysCtrl.reboot();
        }

        @Override
        public int beep(int frequncy, int timeMs) throws RemoteException {
            return mSpSysCtrl.beep(frequncy, timeMs);
        }

        @Override
        public int ledControl(int ledName, int ledMode) throws RemoteException {
            return mSpSysCtrl.ledControl(ledName, ledMode);
        }

        @Override
        public byte[] getSn() throws RemoteException {
            //Context mContext;
            //mContext.enforceCallingOrSelfPermission("android.permission.ENTERPRISE_SETTINGS", null);
            return mSpSysCtrl.getSn();
        }

        @Override
        public int setSn(byte[] sn) throws RemoteException {
            return mSpSysCtrl.setSn(sn);
        }

        @Override
        public int spLogOpen() throws RemoteException {
            return mSpSysCtrl.spLogOpen();
        }

        @Override
        public int spLogClose() throws RemoteException {
            return mSpSysCtrl.spLogClose();
        }

        @Override
        public int exeRootCmd(String cmdStr) throws RemoteException {
            return mSpSysCtrl.exeRootCmd(cmdStr);
        }

        @Override
        public int exeCmd(String cmdStr) throws RemoteException {
            return mSpSysCtrl.exeCmd(cmdStr);
        }
    };

    private final IIccReader.Stub mIIccReader = new IIccReader.Stub(){
        @Override
        public int open(byte slot, boolean emvMode) throws RemoteException {
            return mIccReader.open(slot, emvMode);
        }

        @Override
        public int close() throws RemoteException {
            return mIccReader.close();
        }

        @Override
        public int check(int timeoutMs) throws RemoteException {
            return mIccReader.check(timeoutMs);
        }

        @Override
        public int checkStop() throws RemoteException {
            return mIccReader.checkStop();
        }

    };

    private final IPsamReader.Stub mIPsamReader = new IPsamReader.Stub(){
        @Override
        public int open(byte slot, boolean emvMode) throws RemoteException {
            return 0;
        }

        @Override
        public int close() throws RemoteException {
            return mPsamReader.close();
        }

        @Override
        public int checkStop() throws RemoteException {
            return mPsamReader.checkStop();
        }

        @Override
        public int disable() throws RemoteException {
            return mPsamReader.disable();
        }


    };

    private final IPiccReader.Stub mIPiccReader = new IPiccReader.Stub(){
        @Override
        public int open(byte slot) throws RemoteException {
            return mPiccReader.open(slot);
        }

        @Override
        public int close() throws RemoteException {
            return mPiccReader.close();
        }

        @Override
        public int search(byte pollMode, byte cardType, int timeoutMs) throws RemoteException {
            return mPiccReader.search(pollMode, cardType, timeoutMs);
        }

        @Override
        public int checkCardType(int cardType) throws RemoteException {
            return mPiccReader.checkCardType(cardType);
        }

        @Override
        public void searchStop() throws RemoteException {
            mPiccReader.searchStop();
        }

        @Override
        public int remove() throws RemoteException {
            return mPiccReader.remove();
        }

        @Override
        public boolean checkIfRemoved() throws RemoteException {
            return mPiccReader.checkIfRemoved();
        }

        @Override
        public int m1Authentication(byte blockNumber, byte keyType, byte[] key) throws RemoteException {
            return mPiccReader.m1Authentication(blockNumber, keyType, key);
        }

        @Override
        public byte[] m1BlockDataRead(byte blockNumber) throws RemoteException {
            return mPiccReader.m1BlockDataRead(blockNumber);
        }

        @Override
        public int m1BlockDataWrite(byte blockNumber, byte[] inData) throws RemoteException {
            return mPiccReader.m1BlockDataWrite(blockNumber, inData);
        }

        @Override
        public int m1ValueDataSet(byte blockNumber, int value) throws RemoteException {
            return mPiccReader.m1ValueDataSet(blockNumber, value);
        }

        @Override
        public int m1ValueDataRead(byte blockNumber, int[] outValue) throws RemoteException {
            return 0;
        }

        @Override
        public int m1ValueDataAdd(byte blockNumber, int value) throws RemoteException {
            return mPiccReader.m1ValueDataAdd(blockNumber, value);
        }

        @Override
        public int m1ValueDataDel(byte blockNumber, int value) throws RemoteException {
            return mPiccReader.m1ValueDataDel(blockNumber, value);
        }

        @Override
        public int m1ValueDataSaveOpRet(byte blockNumber) throws RemoteException {
            return mPiccReader.m1ValueDataSaveOpRet(blockNumber);
        }

        @Override
        public int m1ValueDataUnloading(byte blockNumber) throws RemoteException {
            return mPiccReader.m1ValueDataUnloading(blockNumber);
        }

        @Override
        public byte[] getConfig() throws RemoteException {
            return mPiccReader.getConfig();
        }

        @Override
        public int setConfig(byte printConf, byte[] abCardValidRegValue) throws RemoteException {
            return mPiccReader.setConfig(printConf, abCardValidRegValue);
        }
    };

    private final IMagcardReader.Stub mIMagcardReader = new IMagcardReader.Stub(){
        @Override
        public int open() throws RemoteException {
            return mMagcardReader.open();
        }

        @Override
        public int close() throws RemoteException {
            return mMagcardReader.close();
        }

        @Override
        public boolean detect() throws RemoteException {
            return mMagcardReader.detect();
        }

        @Override
        public int setCheckLrc(boolean value) throws RemoteException {
            return mMagcardReader.setCheckLrc(value);
        }

        @Override
        public byte[] getCardNuber() throws RemoteException {
            return mMagcardReader.getCardNuber();
        }
    };

    private final IPedReader.Stub mIPedReader = new IPedReader.Stub(){
        @Override
        public String hello(String inStr) throws RemoteException {
            Log.d(TAG, "test: inStr =" + inStr);
            return inStr;
        }

        @Override
        public byte[] getRandom(int randomLen) throws RemoteException {
            return mPedReader.getRandom(randomLen);
        }

        @Override
        public int deleteKey(int keyType, int keyIndex) throws RemoteException {
            return mPedReader.deleteKey(keyType, keyIndex);
        }

        @Override
        public int getSensitiveTime(int sensitiveType) throws RemoteException {
            return mPedReader.getSensitiveTime(sensitiveType);
        }

        @Override
        public int setPinInputTimeout(int timeoutSc) throws RemoteException {
            return mPedReader.setPinInputTimeout(timeoutSc);
        }

        @Override
        public int writePinKey(int mode, int mastKeyId, int destKeyId, byte[] keyData) throws RemoteException {
            return mPedReader.writePinKey(mode, mastKeyId, destKeyId, keyData);
        }

        @Override
        public int writeMacKey(int mode, int mastKeyId, int destKeyId, byte[] keyData) throws RemoteException {
            return mPedReader.writeMacKey(mode, mastKeyId, destKeyId, keyData);
        }

        @Override
        public int writeMasterKey(int mode, int mastKeyId, int destKeyId, byte[] keyData) throws RemoteException {
            return mPedReader.writeMasterKey(mode,mastKeyId, destKeyId, keyData);
        }

        @Override
        public int getPinCancel() throws RemoteException {
            return mPedReader.getPinCancel();
        }

        @Override
        public boolean isGetPinIng() throws RemoteException {
            return mPedReader.isGetPinIng();
        }

        @Override
        public byte[] getMac(int keyIndex, int macMode, byte[] inMacData) throws RemoteException {
            return mPedReader.getMac(keyIndex, macMode, inMacData);
        }

        @Override
        public int writeDataKey(int mode, int dataKeyId, int desKeyId, byte[] keyData) throws RemoteException {
            return mPedReader.writeDataKey(mode, dataKeyId, desKeyId, keyData);
        }

        @Override
        public byte[] dataEncrypt(int keyIndex, int dataMode, byte[] inData) throws RemoteException {
            return mPedReader.dataEncrypt(keyIndex, dataMode, inData);
        }

        @Override
        public int snKey(byte[] snKey) throws RemoteException {
            return mPedReader.snKey(snKey);
        }

        @Override
        public int selfKeyCheck() throws RemoteException {
            return mPedReader.selfKeyCheck();
        }

        @Override
        public byte[] snEncrypt(byte[] sn) throws RemoteException {
            return mPedReader.snEncrypt(sn);
        }

        @Override
        public int hsmDeleteAll(byte[] pPIN) throws RemoteException {
            return mPedReader.hsmDeleteAll(pPIN);
        }

        @Override
        public byte[] hsmQueryName(int objectType, int dataType) throws RemoteException {
            return mPedReader.hsmQueryName(objectType, dataType);
        }

        @Override
        public int hsmGetFreeSpace() throws RemoteException {
            return mPedReader.hsmGetFreeSpace();
        }

        @Override
        public int injectDesKey(int desKeyId, byte[] desKey) throws RemoteException {
            return mPedReader.injectDesKey(desKeyId, desKey);
        }

        @Override
        public byte[] calcWkeyKcv(int keyType, int keyIndex) throws RemoteException {
            return mPedReader.calcWkeyKcv(keyType,keyIndex);
        }

        @Override
        public byte[] tdea(int mode, int keyType, int keyIndex, byte[] IV, byte[] inBuff) throws RemoteException {
            return mPedReader.tdea(mode, keyType, keyIndex,IV, inBuff);
        }

        @Override
        public int getKeyLen(int keyType, int keyIndex) throws RemoteException {
            return mPedReader.getKeyLen(keyType, keyIndex);
        }

        @Override
        public int deleteKeys(String pkgName) throws RemoteException {
            return mPedReader.deleteKeys(pkgName);
        }

        @Override
        public int isKeyExist(String pkgName) throws RemoteException {
            return mPedReader.isKeyExist(pkgName);
        }

        @Override
        public int writeAesPinKey(int mode, int mastKeyId, int desKeyId, byte[] keyData) throws RemoteException {
            return mPedReader.writeAesPinKey(mode, mastKeyId, desKeyId, keyData);
        }

        @Override
        public byte[] calcCmacKey(byte[] mastKey, byte[] inKey) throws RemoteException {
            return mPedReader.calcCmacKey(mastKey, inKey);
        }

        @Override
        public int savePassword(int index, byte[] password) throws RemoteException {
            return mPedReader.savePassword(index, password);
        }

        @Override
        public int checkPassword(int index, byte[] password) throws RemoteException {
            return mPedReader.checkPassword(index, password);
        }

        @Override
        public byte[] tdeaDencrypt(int mode, byte[] password, byte[] data) throws RemoteException {
            return mPedReader.tdeaDencrypt(mode, password, data);
        }

        @Override
        public int syncTime() throws RemoteException {
            return mPedReader.syncTime();
        }

        @Override
        public int importPin(byte endKey, byte[] inPinData) throws RemoteException {
            return mPedReader.importPin(endKey, inPinData);
        }

        @Override
        public int fsFormat(int timeoutMs) throws RemoteException {
            return mPedReader.fsFormat(timeoutMs);
        }

        @Override
        public int pedFormat(int timeoutMs) throws RemoteException {
            return mPedReader.pedFormat();
        }

        @Override
        public int secMaintainCheck() throws RemoteException {
            return mPedReader.secMaintainCheck();
        }

        @Override
        public int secMaintainActivate(byte[] actPara) throws RemoteException {
            return mPedReader.secMaintainActivate(actPara);
        }


    };

    private final ISpDownloader.Stub mISpDownloader = new ISpDownloader.Stub(){
        @Override
        public int open() throws RemoteException {
            return mSpDownloader.open();
        }

        @Override
        public int close() throws RemoteException {
            return mSpDownloader.close();
        }

        @Override
        public int down(byte[] writeByte) throws RemoteException {
            return mSpDownloader.down(writeByte);
        }

        @Override
        public int waitForUpdateFinish(int timeoutMs) throws RemoteException {
            return mSpDownloader.waitForUpdateFinish(timeoutMs);
        }

        @Override
        public String getVersion() throws RemoteException {
            return mSpDownloader.getVersion();
        }

    };

}
