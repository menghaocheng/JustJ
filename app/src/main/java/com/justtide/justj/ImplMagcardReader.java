package com.justtide.justj;

import android.util.Log;

import com.just.api.BcmDrvListener.OnJustUeventListener;
import com.just.api.Device;
import com.just.api.PosDevice;
import com.just.api.UtilFun;
import com.justtide.aidl.IMagcardReader;
import com.justtide.aidl.MagneticCard;

public final class ImplMagcardReader extends IMagcardReader.Stub implements OnJustUeventListener
{
	private static final String TAG = "ImplMagcardReader";
    private static boolean dbg = false;

    public static final int MAGCARD_ERR_BASE = PosDevice.POS_ERR_MAGCARD_BASE;
	public static final int MAGCARD_ERR_SUCCESS            = 0;
	public static final int MAGCARD_ERR_CONNECT_FAILED     = MAGCARD_ERR_BASE - 1;
	public static final int MAGCARD_ERR_CMD_TIMEOUT        = MAGCARD_ERR_BASE - 2;
	public static final int MAGCARD_ERR_SWIPE_ERR          = MAGCARD_ERR_BASE - 3;
	public static final int MAGCARD_ERR_INVALID_TRACK_DATA = MAGCARD_ERR_BASE - 4;
	public static final int MAGCARD_ERR_USER_CANCEL        = MAGCARD_ERR_BASE - 5;
	public static final int MAGCARD_ERR_SWIPE_TIMEOUT      = MAGCARD_ERR_BASE - 6;
	public static final int MAGCARD_ERR_INVAL_PARA         = MAGCARD_ERR_BASE - 7;

	public static final int MAGCARD_STATE_CLOSED = 0;
	public static final int PRINTER_STATE_OPEN = 1;

	private static final int MAGCARD_WIPE_MAG_NUMA = 9527;
	private static final int MAGCARD_WIPE_MAG_NUMB = 9528;

	public static final int MAGCARD_DEFAULT_TIMEOUT = 10000;

	public static final int TRACK_DATA_MAXLEN = 104;

	//private static final int MAX_TRACK_1_LENGTH = 76;
	//private static final int MIN_TRACK_2_LENGTH = 21;
	//private static final int MAX_TRACK_2_LENGTH = 37;
	//private static final int MAX_TRACK_3_LENGTH = 104;

	public static final int USER_CANCEL = -1004;
	public static final int TIMEOUT_ERROR = -1005;
	private boolean isCancel = false;

	PosDevice mPosMagCard = null;

	static boolean mPciFlag = false;

	static PkgMap mPkgMap  = new PkgMap();

	private int mExpValue = MAGCARD_ERR_SUCCESS;

	private int mMagCardState = MAGCARD_STATE_CLOSED;

	private int mMagSwipFlag = MAGCARD_WIPE_MAG_NUMA;

	/** 内部类，用于实现lzay机制，保证线程安全 */
	/*private static class MagcardReaderHolder{
		private static MagcardReader mMagCardReader = new MagcardReader();
	}

	public static MagcardReader getInstance()
	{
		return MagcardReaderHolder.mMagCardReader;
	}

	private ImplMagcardReader()
	{
		mPosMagCard = new PosDevice(Device.DEV_ID_MAG_CARD);
		mPosMagCard.setOnJustUeventListener(this);
		//mPosMagCard.DeviceOpen(0);
	}*/

	public ImplMagcardReader(PosDevice inPosDevice){
		mPosMagCard = inPosDevice;
	}

	/**
	 * 设置是否打开模块日志
	 * @param flag  true:打开，false：关闭
	 */
	public void logOpen(boolean flag){
		dbg = false;
	}

	public void setExpValue(int errCode){
		Log.i(TAG, "setExpValue:" + errCode + expToString(errCode));
		mExpValue = errCode;
	}
	
	/**
	 * get the latest errCode
	 * @return
	 */
	public int getExpValue(){
		return mExpValue;
	}
	
	/**
	 * convert errCode into String
	 * @param errCode
	 * @return
	 */
	public static String expToString(int errCode) {
		String exceptionMessage;
		switch (errCode) {
		case MAGCARD_ERR_SUCCESS:
			exceptionMessage = "MagCard Success!";
			break;
		case MAGCARD_ERR_CONNECT_FAILED:
			exceptionMessage = "Connect To MagCard Failed!";
			break;
		case MAGCARD_ERR_CMD_TIMEOUT:
			exceptionMessage = "MagCard Cmd Timeout";
			break;
		case MAGCARD_ERR_SWIPE_ERR:
			exceptionMessage = "Swipe Error";
			break;
		case MAGCARD_ERR_INVALID_TRACK_DATA:
			exceptionMessage = "Invalid Track Data";
			break;
		case MAGCARD_ERR_USER_CANCEL:
			exceptionMessage = "Canceled";
			break;
		case MAGCARD_ERR_SWIPE_TIMEOUT:
			exceptionMessage = "Magcard Swipe Timeout";
			break;
		case MAGCARD_ERR_INVAL_PARA:
			exceptionMessage = "Invalid Parameter";
			break;
		default:
			exceptionMessage = "Error:" + errCode;
			break;
		}
		
		return exceptionMessage;
	}
	
	/**
	 * convert state into String
	 * @param state
	 * @return
	 */
	private static String stateToString(int state){
		String stateString = "";
		switch (state){
		case MAGCARD_STATE_CLOSED:
			stateString = "MAGCARD_STATE_CLOSED";
			break;
			
		case PRINTER_STATE_OPEN:
			stateString = "PRINTER_STATE_OPEN";
			break;
		default:
			stateString = "Undefined State:" + state;
			break;
		}
		return stateString;
	}
	
	private void setState(int newState){
		Log.i(TAG, "setState:" + stateToString(newState));
		if(newState < MAGCARD_STATE_CLOSED || newState > PRINTER_STATE_OPEN){
			Log.e(TAG, "setState: Ilvalid newState[" + newState + "]");
		}
		mMagCardState = newState;
	}
	
	
	/**
	 * get the current state of magcard
	 * @return
	 * 
	 */
	public int getState() {
		byte[] thisStateBuff = new byte[32];
		int reval = mPosMagCard.magCardGetState(thisStateBuff);
		if (reval != 0){
			return reval;
		}
		mMagCardState = (int)thisStateBuff[8];//UtilFun.bytesToInt(thisStateBuff, 4);
		Log.i(TAG, "mMagCardState="+mMagCardState);
		return mMagCardState;
	}
	
	private void setSwipFlag(){
		mMagSwipFlag = MAGCARD_WIPE_MAG_NUMB;
	}
	
	public void clearSwipFlag(){
		mMagSwipFlag = MAGCARD_WIPE_MAG_NUMA;
	}
	
	/**
	 * Open and power on the magnetic strip card reader.
     * if there is no card swipe within 60 seconds, it will be closed automatically
	 * @return
	 */
	@Override
	public int open() {
		if (dbg) Log.e(TAG, "open ...");
		
		getState();
		//if (currState == MAGCARD_STATE_CLOSED){
		//	mPosMagCard.icCardClose();			
		//}
		int reval = mPosMagCard.magCardOpen();
		if (reval != 0) {
			Log.e(TAG, "open failed: " + expToString(reval));
			mPosMagCard.magCardClose();
			setState(MAGCARD_STATE_CLOSED);
		}
		clearSwipFlag();
		return 0;
	}

	/**
	 * Close and power off the magnetic strip card reader.
	 * @return
	 */
	@Override
    public int close() {
    	if (dbg) Log.d(TAG, "close ...");
    	int reval = mPosMagCard.magCardClose();
		if (reval != 0) {
			Log.e(TAG, "close failed: " + expToString(reval));
		}
		return reval;
    }
    
    /**
     * Reset magnetic strip card reader.
     * @return
     */
	@Override
    public int reset(){
    	if (dbg) Log.d(TAG, "reset ...");
    	int reval = mPosMagCard.magCardReset();
		if (reval != 0) {
			Log.e(TAG, "reset failed: " + expToString(reval));
			return reval;
		}
		return 0;
    }

    /**
     * check if is there any magnetic card swiped
     * @return
     */
	@Override
    public boolean detect(){
    	return mMagSwipFlag == MAGCARD_WIPE_MAG_NUMB;
    }
    
    public void cancel(){
    	isCancel = true;
    }

	public int setPkgName(String pkgName){
		if (pkgName == null){
			return MAGCARD_ERR_INVAL_PARA;
		}
		int reval = mPosMagCard.pedSetPkgName(pkgName.getBytes());
		if (reval < 0){
			Log.e(TAG, "pedSetPkgName failed. reval=" + reval);
			return reval;
		}
		return 0;
	}

    /**
     * Read magnetic card number after detected a card swiped.
     * @return
     */
	@Override
	public MagneticCard read(){
		if (dbg) Log.d(TAG, "read ...");
		int reval = 0;
		String[] trackData = {"","",""};
		int nReadResult = 0;
		
		byte[] mcrTrackAll = new byte[450];
		reval = mPosMagCard.magCardRead(mcrTrackAll);
		if (reval < 0){
			Log.e(TAG, "read failed: " + expToString(reval));
			setExpValue(reval);
			return null;
		}
		
		//int rc = UtilFun.bytesToInt(mcrTrackAll, 4);
		
		int offset = 8;
		int trackLength = 0;
		
		//chanel1:
		trackLength = mcrTrackAll[offset] & 0xff;
		offset ++;
		if (trackLength > TRACK_DATA_MAXLEN){
			Log.e(TAG, "read:" + expToString(MAGCARD_ERR_INVALID_TRACK_DATA));
			setExpValue(MAGCARD_ERR_INVALID_TRACK_DATA);
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
		if (trackLength > TRACK_DATA_MAXLEN){
			Log.e(TAG, "read:" + expToString(MAGCARD_ERR_INVALID_TRACK_DATA));
			setExpValue(MAGCARD_ERR_INVALID_TRACK_DATA);
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
		if (trackLength > TRACK_DATA_MAXLEN){
			Log.e(TAG, "read:" + expToString(MAGCARD_ERR_INVALID_TRACK_DATA));
			setExpValue(MAGCARD_ERR_INVALID_TRACK_DATA);
			return null;
		}
		String chanelStr3 = UtilFun.byteToString(mcrTrackAll, offset, trackLength);
		trackData[2] = chanelStr3;
		offset += trackLength;
		
		if (trackLength > 0) {
			nReadResult |= 0x04;
		}

		clearSwipFlag();
		return new MagneticCard(trackData,nReadResult);
	}

	/**
	 *
	 * @param value
	 * @return
	 */
	@Override
	public int setCheckLrc(boolean value){
		if (dbg) Log.i(TAG, "setCheckLrc(" + value + ")");

		int thisValue = 0;
		if (value == true){
			thisValue = 1;
		}
		int reval = mPosMagCard.magSetCheckLrc(thisValue);
		if (reval != 0) {
			Log.e(TAG, "magSetCheckLrc failed: " + expToString(reval));
			return reval;
		}
		return 0;
	}

	@Override
	public byte[] getCardNuber(){
		if (dbg) Log.i(TAG, "getCardNumber()");
		byte[] thisBytes = new byte[74];

		int reval = mPosMagCard.magGetCardNumber(thisBytes);
		if (reval != 0) {
			Log.e(TAG, "magGetCardNumber failed: " + expToString(reval));
			setExpValue(reval);
			return null;
		}
		int cardNumLen = UtilFun.bytesToInt32(thisBytes, 0) - 4;
		byte[] outByte = new byte[cardNumLen];
		System.arraycopy(thisBytes, 8, outByte, 0, cardNumLen);
		return outByte;
	}
	
	//========================================================================
	
	@Override
	public void OnJustUevent(int what, int arg, byte[] buff){
		if (what < PosDevice.POS_CMD_MAGCARD_BASE || what > PosDevice.POS_CMD_MAGCARD_MAX){
			return;
		}
		Log.i(TAG, "OnJustUevent:" + what + ',' + arg);
		switch(what){
		case PosDevice.POS_UNSOLI_MAGCARD_STATE_CHANGE:
			Log.i(TAG, "POS_UNSOLI_MAGCARD_STATE_CHANGE");
			int newState = arg;
			setState(newState);
			break;
		case PosDevice.POS_UNSOLI_MAGCARD_SWIPING:
			Log.i(TAG, "OnDeviceStateChange:POS_UNSOLI_MAGCARD_SWIPING");
			setSwipFlag();
			/*
			 * if(mMagSwipFlag == MAGCARD_WIPE_MAG_NUMA){
			 *
				mMagSwipFlag = MAGCARD_WIPE_MAG_NUMB;
			}
			*/
			break;
		default:
			//Log.e(TAG, "OnJustUevent:Undefined event[" + what + "]");
			break;
		}
	}

}

