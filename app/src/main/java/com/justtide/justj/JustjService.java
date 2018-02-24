package com.justtide.justj;

import com.just.api.PosDevice;
import com.just.api.UtilFun;
import com.justtide.aidl.AidlJustjService;
import com.justtide.aidl.CommandApdu;
import com.justtide.aidl.ContactCard;
import com.justtide.aidl.ContactlessCard;
import com.justtide.aidl.HsmObj;
import com.justtide.aidl.IGuomiReader;
import com.justtide.aidl.IIccReader;
import com.justtide.aidl.IMagcardReader;
import com.justtide.aidl.IPedReader;
import com.justtide.aidl.IPiccReader;
import com.justtide.aidl.IPsamReader;
import com.justtide.aidl.ISpDownloader;
import com.justtide.aidl.ISpSysCtrl;
import com.justtide.aidl.IThermalPrinter;
import com.justtide.aidl.MagneticCard;
import com.justtide.aidl.PiccInterface;
import com.justtide.aidl.ResponseApdu;
import com.justtide.aidl.RsaPinKey;
import com.justtide.aidl.Task;
import com.justtide.aidl.TaskType;
import com.justtide.aidl.Time;

//import com.justtide.justtide.ContactlessCard;
import com.justtide.justtide.GuomiReader;
import com.justtide.justtide.IccReader;
import com.justtide.justtide.MagcardReader;
import com.justtide.justtide.PiccReader;
import com.justtide.justtide.PsamReader;
import com.justtide.justtide.SpDownloader;
import com.justtide.justtide.SpSysCtrl;
import com.justtide.justtide.PedReader;
import com.justtide.justtide.ThermalPrinter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import android.support.annotation.Nullable;
import android.util.Log;

import static com.justtide.justtide.MagcardReader.MAGCARD_ERR_INVALID_TRACK_DATA;

public class JustjService extends Service {

    private static final String TAG = "JustjService";
    private static boolean dbg = false;


    PosDevice mPosDevice;

    SpSysCtrl mSpSysCtrl;
    IccReader mIccReader;
    PsamReader mPsamReader;
    PiccReader mPiccReader;
    MagcardReader mMagcardReader;
    ThermalPrinter mThermalPrinter;
    PedReader mPedReader;
    SpDownloader mSpDownloader;
    GuomiReader mGuomiReader;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();

        mPosDevice = new PosDevice(-1);

        mSpSysCtrl = SpSysCtrl.getInstance();
        mIccReader = IccReader.getInstance();
        mPsamReader = PsamReader.getInstance();
        mPiccReader = PiccReader.getInstance();
        mMagcardReader = MagcardReader.getInstance();
        mThermalPrinter = ThermalPrinter.getInstance();
        mPedReader = PedReader.getInstance(null);
        mSpDownloader = SpDownloader.getInstance();
        mGuomiReader = GuomiReader.getInstance();

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
        public IBinder getMagcardReader() throws RemoteException {
            return mIMagcardReader;
        }

        @Override
        public IBinder getPiccReader() throws RemoteException {
            return mIPiccReader;
        }

        @Override
        public IBinder getThermalPrinter() throws RemoteException {
            return mIThermalPrinter;
        }

        @Override
        public IBinder getPinPad(int devid) throws RemoteException {
            return mIPedReader;
        }

        @Override
        public IBinder getDownload() throws RemoteException {
            return mISpDownloader;
        }

        @Override
        public IBinder getGuomiReader() throws RemoteException {
            return mIGuomiReader;
        }
    };

    private final ISpSysCtrl.Stub mISpSysCtrl = new ISpSysCtrl.Stub(){
        @Override
        public int getExpValue() throws RemoteException {
            return mGuomiReader.getExpValue();
        }

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
        public int setTime(Time time) throws RemoteException {
            byte[] thisByte = new byte[0];
            if(time != null){
                thisByte = time.getBytes();
            }
            int reval = mPosDevice.sysSetTime(thisByte);
            if (reval < 0){
                Log.e(TAG, "sysSetTime failed" + SpSysCtrl.expToString(reval));
                return reval;
            }
            return 0;
        }

       @Override
        public Time getTime() throws RemoteException {
            byte[] thisByte = new byte[256];
            int reval = mPosDevice.sysGetTime(thisByte);
            if (reval < 0){
                Log.e(TAG, "sysGetTime failed:" + SpSysCtrl.expToString(reval));
                mSpSysCtrl.setExpValue(reval);
                return null;
            }
            return new com.justtide.aidl.Time(thisByte, 8);
        }

        /*
        @Override
        public int setTime(long timestamp) throws RemoteException {
            return 0;
        }

        @Override
        public long getTime() throws RemoteException {
            return 0;
        }*/

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
        public int getExpValue() throws RemoteException {
            return mGuomiReader.getExpValue();
        }

        @Override
        public void setExpValue(int errCode) throws RemoteException {
            mGuomiReader.setExpValue(errCode);
        }

        @Override
        public int open(byte slot, boolean emvMode) throws RemoteException {
            return mIccReader.open(slot, emvMode);
        }

        @Override
        public int close() throws RemoteException {
            return mIccReader.close();
        }

        @Override
        public ContactCard enable(byte slot, byte vccMode, boolean emvMode) throws RemoteException {
            byte[] atrTemp = new byte[40];
            int reval = mPosDevice.icCardActivate(vccMode, emvMode, atrTemp);
            if (reval != 0){
                Log.e(TAG, "enable3:" + IccReader.expToString(reval));
                setExpValue(reval);
                return null;
            }

            //int artLength = PosDevice.getDataBuffLen(atrTemp) - 4;
            int artLength = UtilFun.bytesToInt32(atrTemp, 0) - 4;
            byte[] atr = new byte[artLength];
            System.arraycopy(atrTemp, 8, atr, 0, artLength);

            com.justtide.aidl.ContactCard contactCard = new com.justtide.aidl.ContactCard(slot, vccMode, emvMode, atr);
            //contactCard.setState(com.justtide.aidl.ContactCard.IS_ENABLED);
            return contactCard;
        }

        @Override
        public int disable() throws RemoteException {
            return mIccReader.disable();
        }

        @Override
        public int check(int timeoutMs) throws RemoteException {
            return mIccReader.check(timeoutMs);
        }

        @Override
        public int checkStop() throws RemoteException {
            return mIccReader.checkStop();
        }

        @Override
        public ResponseApdu transmit(ContactCard contactCard, CommandApdu command) throws RemoteException {
            byte[] apdu = command.getTransmitBytes();
            byte[] responseBytes = new byte[512];

            int reval = mPosDevice.icCardApdu(apdu, responseBytes);
            if (reval != 0) {
                Log.e(TAG,"transmit:icCardApdu() failed:"+ IccReader.expToString(reval));
                //contactCard.setState(com.justtide.aidl.ContactCard.IS_ABSENT);
                return null;
            }

            int apduLen = UtilFun.bytesToInt32(responseBytes, 0) - 4;
            if (apduLen < 2){
                Log.e(TAG, "invalid apdu rsp:len="+apduLen);
                setExpValue(IccReader.ICCARD_ERR_APDU);
                return null;
            }
            byte[] apduRsp = new byte[apduLen];
            System.arraycopy(responseBytes, 8, apduRsp, 0, apduLen);
            return new com.justtide.aidl.ResponseApdu(apduRsp);
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

        @Override
        public ContactCard enable(byte slot, byte vccMode, boolean emvMode) throws RemoteException {
            byte[] atrTemp = new byte[40];
            int reval = mPosDevice.psamActivate(vccMode, emvMode, atrTemp);
            if (reval != 0){
                Log.e(TAG, "psamActivate failed:" + PsamReader.expToString(reval));
                mPsamReader.setExpValue(reval);
                return null;
            }

            int artLength = UtilFun.bytesToInt32(atrTemp, 0) - 4;
            byte[] atr = new byte[artLength];
            System.arraycopy(atrTemp, 8, atr, 0, artLength);

            com.justtide.aidl.ContactCard contactCard = new com.justtide.aidl.ContactCard(slot, vccMode, emvMode, atr);
            return contactCard;
        }

        @Override
        public ResponseApdu transmit(ContactCard contactCard, CommandApdu command) throws RemoteException {
            byte[] apdu = command.getTransmitBytes();
            byte[] responseBytes = new byte[512];

            int reval = mPosDevice.psamApdu(apdu, responseBytes);
            if (reval != 0) {
                Log.e(TAG,"transmit:psamApdu() failed:"+ PsamReader.expToString(reval));
                //contactCard.setState(com.justtide.aidl.ContactCard.IS_ABSENT);
                return null;
            }

            int apduLen = UtilFun.bytesToInt32(responseBytes, 0) - 4;
            if (apduLen < 2){
                Log.e(TAG, "invalid apdu rsp:len="+apduLen);
                mPsamReader.setExpValue(PsamReader.PSAM_ERR_APDU);
                return null;
            }
            byte[] apduRsp = new byte[apduLen];
            System.arraycopy(responseBytes, 8, apduRsp, 0, apduLen);
            return new com.justtide.aidl.ResponseApdu(apduRsp);
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

    private final IPiccReader.Stub mIPiccReader = new IPiccReader.Stub(){
        @Override
        public int getExpValue() throws RemoteException {
            return mGuomiReader.getExpValue();
        }

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
        public com.justtide.aidl.ContactlessCard detect() throws RemoteException {
            int reval = 0;

            if (mPiccReader.mNfcSwipeFlag == PiccReader.NFCCARD_SWIPE_MAG_NUMA){
                return null;
            }

            if(mPiccReader.mNfcState != PiccReader.NFCCARD_STATE_READY){
                return null;
            }
            byte[] thisStateBuff = new byte[64];
            reval = mPosDevice.nfcCardGetState(thisStateBuff);
            if (reval != 0){
                mPiccReader.setExpValue(reval);
                return null;
            }

            return new ContactlessCard(thisStateBuff, 9);
        }

        /*@Override
        public int detect(PiccInterface piccInterface) throws RemoteException {
            int reval = 0;

            if (mPiccReader.mNfcSwipeFlag == PiccReader.NFCCARD_SWIPE_MAG_NUMA){
                return 1;
            }

            if(mPiccReader.mNfcState != PiccReader.NFCCARD_STATE_READY){
                return getExpValue();
            }
            byte[] thisStateBuff = new byte[64];
            reval = mPosDevice.nfcCardGetState(thisStateBuff);
            if (reval != 0){
                return reval;
            }

            ContactlessCard contactlessCard = new ContactlessCard(thisStateBuff, 9);
            piccInterface.getContactlessCard(reval, contactlessCard);

            return 0;
        }*/

        @Override
        public int checkCardType(int cardType) throws RemoteException {
            return mPiccReader.checkCardType(cardType);
        }

        @Override
        public void searchStop() throws RemoteException {
            mPiccReader.searchStop();
        }

        @Override
        public ResponseApdu transmit(CommandApdu command, byte[] exPara) throws RemoteException {
            byte[] commandBytes = command.getTransmitBytes();
            byte[] responseBytes = new byte[550];

            int reval = mPosDevice.nfcCardApdu(commandBytes, exPara, responseBytes);
            if (reval != 0) {
                Log.e(TAG,"transmit:nfcCardApdu() failed:"+ PiccReader.expToString(reval));
                mPiccReader.setExpValue(reval);
                return null;
            }

            int apduLen = UtilFun.bytesToInt32(responseBytes, 0) - 4;
            if (apduLen < 2){
                Log.e(TAG, "invalid apdu rsp:len="+apduLen);
                mPiccReader.setExpValue(PiccReader.NFCCARD_ERR_APDU);
                return null;
            }
            byte[] apduRsp = new byte[apduLen];
            System.arraycopy(responseBytes, 8, apduRsp, 0, apduLen);
            return new com.justtide.aidl.ResponseApdu(apduRsp);
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
        public int getExpValue() throws RemoteException {
            return mGuomiReader.getExpValue();
        }

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
        public int reset() throws RemoteException {
            return mMagcardReader.reset();
        }

        @Override
        public MagneticCard read() throws RemoteException {
            int reval = 0;
            String[] trackData = {"","",""};
            int nReadResult = 0;

            byte[] mcrTrackAll = new byte[450];
            reval = mPosDevice.magCardRead(mcrTrackAll);
            if (reval < 0){
                Log.e(TAG, "read failed: " + mMagcardReader.expToString(reval));
                mMagcardReader.setExpValue(reval);
                return null;
            }

            //int rc = UtilFun.bytesToInt(mcrTrackAll, 4);

            int offset = 8;
            int trackLength = 0;

            //chanel1:
            trackLength = mcrTrackAll[offset] & 0xff;
            offset ++;
            if (trackLength > MagcardReader.TRACK_DATA_MAXLEN){
                Log.e(TAG, "read:" + mMagcardReader.expToString(MAGCARD_ERR_INVALID_TRACK_DATA));
                mMagcardReader.setExpValue(MAGCARD_ERR_INVALID_TRACK_DATA);
                return null;
            }
            String chanelStr1 = UtilFun.byteToString(mcrTrackAll, offset, trackLength);
            trackData[0] = chanelStr1;
            offset += trackLength;

            if (trackLength > 0) {
                nReadResult |= 0x01;
            }

            //chanel2:
            trackLength = mcrTrackAll[offset] & 0xff;
            offset ++;
            if (trackLength > MagcardReader.TRACK_DATA_MAXLEN){
                Log.e(TAG, "read:" + mMagcardReader.expToString(MAGCARD_ERR_INVALID_TRACK_DATA));
                mMagcardReader.setExpValue(MAGCARD_ERR_INVALID_TRACK_DATA);
                return null;
            }
            String chanelStr2 = UtilFun.byteToString(mcrTrackAll, offset, trackLength);
            trackData[1] = chanelStr2;
            offset += trackLength;

            if (trackLength > 0) {
                nReadResult |= 0x02;
            }

            //chanel3:
            trackLength = mcrTrackAll[offset] & 0xff;
            offset ++;
            if (trackLength > MagcardReader.TRACK_DATA_MAXLEN){
                Log.e(TAG, "read:" + mMagcardReader.expToString(MAGCARD_ERR_INVALID_TRACK_DATA));
                mMagcardReader.setExpValue(MAGCARD_ERR_INVALID_TRACK_DATA);
                return null;
            }
            String chanelStr3 = UtilFun.byteToString(mcrTrackAll, offset, trackLength);
            trackData[2] = chanelStr3;
            offset += trackLength;

            if (trackLength > 0) {
                nReadResult |= 0x04;
            }

            mMagcardReader.clearSwipFlag();
            return new com.justtide.aidl.MagneticCard(trackData,nReadResult);
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

    private final IThermalPrinter.Stub mIThermalPrinter = new IThermalPrinter.Stub(){
        @Override
        public int getExpValue() throws RemoteException {
            return mGuomiReader.getExpValue();
        }

        @Override
        public int open() throws RemoteException {
            return mThermalPrinter.open();
        }

        @Override
        public int close() throws RemoteException {
            return mThermalPrinter.close();
        }

        @Override
        public int print(byte[] writeByte) throws RemoteException {
            return mThermalPrinter.print(writeByte);
        }

        @Override
        public void cancel() throws RemoteException {
            mThermalPrinter.cancel();
        }

        @Override
        public boolean isFinished() throws RemoteException {
            return mThermalPrinter.isFinished();
        }

        @Override
        public int waitForPrintFinish(int timeoutMs) throws RemoteException {
            return mThermalPrinter.waitForPrintFinish(timeoutMs);
        }

    };

    private final IPedReader.Stub mIPedReader = new IPedReader.Stub(){

        @Override
        public int getExpValue() throws RemoteException {
            return mGuomiReader.getExpValue();
        }

        @Override
        public void setExpValue(int errCode) throws RemoteException {
            mGuomiReader.setExpValue(errCode);
        }

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
        public byte[] getPin(int keyIndex, int pinMode, byte[] cardNo, byte[] expectPinLenList, String priceStr, String title) throws RemoteException {
            return mPedReader.getPin(keyIndex, pinMode, cardNo, expectPinLenList, null, priceStr,title);
        }

        @Override
        public byte[] iccOfflinePlainPin(byte[] expectPinLenList, byte[] apduData, String pricStr, String title) throws RemoteException {
            return mPedReader.iccOfflinePlainPin(expectPinLenList, apduData, null, pricStr, title);
        }

        @Override
        public byte[] iccOfflineCipherPin(byte[] expectPinLenList, byte[] apduData, RsaPinKey rsaPinKey, String pricStr, String title) throws RemoteException {
            //return mPedReader.iccOfflineCipherPin(expectPinLenList, apduData, rsaPinKey, null, pricStr, title);
            int cp = 0;

            int[] thisIntArray = UtilFun.getIntArrayFromByte(expectPinLenList);
            if (thisIntArray == null){
                setExpValue(PedReader.PED_ERR_INVAL_PARA);
                return null;
            }

            if(apduData.length != 6){
                Log.e(TAG, "iccOfflineCipherPin: apduData.length != 6");
                setExpValue(PedReader.PED_ERR_INVAL_PARA);
                return null;
            }

            int reval = mPedReader.confirmPkgName();
            if (reval < 0){
                setExpValue(reval);
                return null;
            }

            byte[] random10 = mPedReader.getRandom10(10);
            if (random10 == null){
                setExpValue(PedReader.PED_ERR_GET_RANDOM_FAIL);
                return null;
            }

            byte[] rsaPinKeyByte = rsaPinKey.getBytes();
            byte[] thisIccData = new byte[apduData.length + rsaPinKeyByte.length];
            System.arraycopy(apduData, 0, thisIccData, cp, apduData.length);
            cp += apduData.length;
            System.arraycopy(rsaPinKeyByte, 0, thisIccData, cp, rsaPinKeyByte.length);

            Log.e(TAG, "iccOfflineCipherPin: thisIccData.leng=" + thisIccData.length);

            mPedReader.mExpValue = 0;
            reval = mPosDevice.pedIccOfflineCipherPin(expectPinLenList,thisIccData);
            if (reval < 0){
                setExpValue(reval);
                Log.e(TAG, "pedIccOfflineCipherPin failed. reval="+reval);
                return null;
            }

            if (mPedReader.mExpValue != 0){
                return null;
            }

            reval = mPosDevice.pedStartPinBlock(expectPinLenList, random10, title, pricStr);
            if (reval < 0){
                Log.e(TAG, "pedStartPinBlock failed: reval = " + reval );
                setExpValue(PedReader.PED_ERR_SART_PINBLOCK_FAILED);
                return null;
            }
            long start = System.currentTimeMillis();
            long end = 0;

            mPedReader.mIccOfflineCihperBlock = null;
            mPedReader.mIccOfflineCipherFlag = PedReader.PED_FLAG_MAG_NUMA;
            mPedReader.mCancelFlag = false;
            while (true) {
                end = System.currentTimeMillis();
                if ((end - start) >= (mPedReader.mPinTimeout + 200)) {
                    Log.e(TAG, "pedIccOfflineCipherPin timeout:" + mPedReader.mPinTimeout + "ms");
                    setExpValue(PedReader.PED_RET_TIMEOUT);
                    mPedReader.mIccOfflineCipherFlag = PedReader.PED_FLAG_MAG_NUMB;
                    return null;
                }
                if (mPedReader.mIccOfflineCipherFlag != PedReader.PED_FLAG_MAG_NUMA){
                    if(mPedReader.mIccOfflineCihperBlock == null){
                        return null;
                    }
                    byte[] rspdata = new byte[mPedReader.mIccOfflineCihperBlock.length];
                    //this part?
                    System.arraycopy(mPedReader.mIccOfflineCihperBlock, 0, rspdata, 0, mPedReader.mIccOfflineCihperBlock.length);
                    UtilFun.clearByte(mPedReader.mIccOfflineCihperBlock);
                    mPedReader.mIccOfflineCihperBlock = null;
                    return rspdata;
                }

                if (mPedReader.mCancelFlag == true){
                    setExpValue(PedReader.PED_ERR_GETPIN_CANCEL);
                    return null;
                }

                try {
                    Thread.currentThread();
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e(TAG, "check: InterruptedException");
                }
            }
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
        public int hsmSave(HsmObj hsmObj, byte[] objectData, int nDataType) throws RemoteException {

            int reval = mPosDevice.pedHsmSave(hsmObj.mIndex, hsmObj.mName, hsmObj.mPassword, hsmObj.mObjectType, objectData, nDataType);
            if(reval < 0) {
                Log.e("PedReader", "pedHsmSave failed: " + PedReader.expToString(reval));
                return reval;
            } else {
                return 0;
            }

        }

        @Override
        public int hsmDelete(HsmObj hsmObj, byte[] pPIN) throws RemoteException {
            int reval = 0;

            int pinLen = 0;
            if (pPIN != null){
                pinLen = pPIN.length;
            }
            byte[] thisPIN = new byte[pinLen];
            System.arraycopy(pPIN, 0, thisPIN, 0, pinLen);
            reval = mPosDevice.pedHsmDelete(hsmObj.mIndex, hsmObj.mName, hsmObj.mPassword, hsmObj.mObjectType, thisPIN);
            if (reval < 0){
                Log.e(TAG, "pedHsmDelete failed: " + PedReader.expToString(reval));
                return reval;
            }
            return 0;
        }

        @Override
        public int hsmDeleteAll(byte[] pPIN) throws RemoteException {
            return mPedReader.hsmDeleteAll(pPIN);
        }

        @Override
        public int hsmQueryCount(HsmObj hsmObj) throws RemoteException {
            int reval = 0;
            byte[] thisByte = new byte[32];
            reval = mPosDevice.pedHsmQueryCount(hsmObj.mIndex, hsmObj.mName, hsmObj.mPassword, hsmObj.mObjectType,thisByte);
            if (reval < 0){
                Log.e(TAG, "pedHsmQueryCount failed: " + PedReader.expToString(reval));
                return reval;
            }
            int thisCount = UtilFun.bytesToInt32(thisByte, 8);
            return thisCount;
        }

        @Override
        public byte[] hsmLoad(int nIndex, HsmObj hsmObj, int nDataType) throws RemoteException {
            byte[] thisOutByte = new byte[1024*4 + 72];
            int reval = mPosDevice.pedHsmLoad(nIndex, hsmObj.mIndex, hsmObj.mName, hsmObj.mPassword, hsmObj.mObjectType, nDataType, thisOutByte);
            if (reval < 0){
                mPedReader.setExpValue(reval);
                Log.e(TAG, "pedHsmLoad failed: " + PedReader.expToString(reval));
                return null;
            }
            //return null;
            int hsmDataLen = UtilFun.bytesToInt32(thisOutByte,0) - 4;
            byte[] outData = new byte[hsmDataLen];
            System.arraycopy(thisOutByte, 8, outData, 0, hsmDataLen);
            return outData;
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

        @Override
        public Task taskWait(int timeoutMs) throws RemoteException {
            long start = System.currentTimeMillis();
            long end = 0;
            Task thisTask = null;

            mPedReader.mTaskWaitFlag = true;
            while(true) {
                if(timeoutMs > 0){
                    end = System.currentTimeMillis();
                    if ( (end - start) >= timeoutMs ) {
                        Log.e(TAG, "taskWait timeout:" + timeoutMs + "ms");
                        return null;
                    }
                }

                if (mPedReader.mStartPinblockFlag == true) {
                    Log.e(TAG, "taskWait: TASK_TYPE_PIN_BLOCK" );
                    try {
                        if(mPedReader.mPricStr == null ){
                            thisTask = new Task(0, TaskType.TASK_TYPE_PIN_BLOCK, mPedReader.mExpectPinLenList, mPedReader.mRandom10);
                        }
                        else if(mPedReader.mPricStr != null && mPedReader.mTitle == null){
                            thisTask = new Task(0, TaskType.TASK_TYPE_PIN_BLOCK, mPedReader.mExpectPinLenList, mPedReader.mRandom10, mPedReader.mPricStr);
                        }
                        else{
                            thisTask = new Task(0, TaskType.TASK_TYPE_PIN_BLOCK, mPedReader.mExpectPinLenList, mPedReader.mRandom10, mPedReader.mPricStr,mPedReader.mTitle);
                        }
                        mPedReader.mTitle = null;
                        mPedReader.mPricStr = null;
                        mPedReader.mExpectPinLenList = null;
                        mPedReader.mRandom10 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    synchronized (this){
                        mPedReader.mStartPinblockFlag = false;
                    }
                    return thisTask;
                }
                if (mPedReader.mHidePinblockFlag == true) {
                    Log.e(TAG, "taskWait: TASK_TYPE_HIDE_PIN_BLOCK" );
                    try {
                        thisTask = new Task(1, TaskType.TASK_TYPE_HIDE_PIN_BLOCK);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    synchronized (this){
                        mPedReader.mHidePinblockFlag = false;
                    }

                    return thisTask;
                }
                if (mPedReader.mStatus != 0) {
                    Log.e(TAG, "taskWait: TASK_TYPE_TRIGGER(" + mPedReader.mStatus + ")" );
                    try {
                        thisTask = new Task(0, TaskType.TASK_TYPE_TRIGGER, mPedReader.mStatus);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mPedReader.mStatus = 0;
                    return thisTask;
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    private final ISpDownloader.Stub mISpDownloader = new ISpDownloader.Stub(){

        @Override
        public int getExpValue() throws RemoteException {
            return mGuomiReader.getExpValue();
        }

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

    private final IGuomiReader.Stub mIGuomiReader = new IGuomiReader.Stub(){

        @Override
        public int getExpValue() throws RemoteException {
            return mGuomiReader.getExpValue();
        }

        @Override
        public void setExpValue(int errCode) throws RemoteException {
            mGuomiReader.setExpValue(errCode);
        }

        @Override
        public String getVersion() throws RemoteException {
            return mGuomiReader.getVersion();
        }

        @Override
        public int open() throws RemoteException {
            return mGuomiReader.open();
        }

        @Override
        public int close() throws RemoteException {
            return mGuomiReader.close();
        }

        @Override
        public ResponseApdu transmit(CommandApdu command, int timeoutMs) throws RemoteException {
            byte[] commandBytes = command.getTransmitBytes();
            byte[] responseBytes = new byte[550];

            int reval = mPosDevice.guomiApdu(commandBytes, timeoutMs, responseBytes);
            if (reval != 0) {
                Log.e(TAG,"guomiApdu() failed:"+ GuomiReader.expToString(reval));
                setExpValue(reval);
                return null;
            }

            int apduLen = UtilFun.bytesToInt32(responseBytes, 0) - 4;
            if (apduLen < 2){
                Log.e(TAG, "invalid apdu rsp:len="+apduLen);
                setExpValue(GuomiReader.GUOMI_ERR_APDU);
                return null;
            }
            byte[] apduRsp = new byte[apduLen];
            System.arraycopy(responseBytes, 8, apduRsp, 0, apduLen);
            return new com.justtide.aidl.ResponseApdu(apduRsp);
        }
    };


}
