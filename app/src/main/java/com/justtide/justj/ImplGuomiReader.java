package com.justtide.justj;

import android.util.Log;

import com.just.api.Device;
import com.just.api.PosDevice;
import com.just.api.UtilFun;
import com.justtide.aidl.CommandApdu;
import com.justtide.aidl.IGuomiReader;
import com.justtide.aidl.ResponseApdu;

public final class ImplGuomiReader extends IGuomiReader.Stub{

	private static final String TAG = "ImplGuomiReader";
	private static boolean dbg = true;

	public static final int GUOMI_ERR_BASE = PosDevice.GUOMI_ERR_BASE;
	public static final int GUOMI_ERR_SUCCESS            = 0;
	public static final int GUOMI_ERR_CONNECT_FAILED     = GUOMI_ERR_BASE - 1;
	public static final int GUOMI_ERR_CMD_TIMEOUT        = GUOMI_ERR_BASE - 2;
	public static final int GUOMI_ERR_APDU               = GUOMI_ERR_BASE - 3;

	public static final int GUOMI_STATE_CLOSED   = 0;
	public static final int GUOMI_STATE_OPEN     = 1;

	PosDevice mPosGuomi = null;

	private int mGuomiState = GUOMI_STATE_CLOSED;

	private int mExpValue = GUOMI_ERR_SUCCESS;

	/*private static class GuomiHolder {
		private static GuomiReader mGuomiReader = new GuomiReader();
	}

	public static GuomiReader getInstance() {
		return GuomiHolder.mGuomiReader;
	}

	private ImplGuomiReader() {
		mPosGuomi = new PosDevice(Device.DEV_ID_GUOMI);
	}
*/

	public ImplGuomiReader(PosDevice inPosDevice){
		mPosGuomi = inPosDevice;
	}

	/**
	 * 设置是否打开模块日志
	 * @param flag  true:打开，false：关闭
	 */
	public void logOpen(boolean flag){
		dbg = false;
	}

	@Override
	public void setExpValue(int errCode){
		Log.i(TAG, "setExpValue:" + errCode +"," + expToString(errCode));
		mExpValue = errCode;
	}

	@Override
	public int getExpValue(){
		return mExpValue;
	}
	
	private static String stateToString(int state){
		String stateString = "";
		switch (state){
		case GUOMI_STATE_CLOSED:
			stateString = "GUOMI_STATE_CLOSED";
			break;
			
		case GUOMI_STATE_OPEN:
			stateString = "GUOMI_STATE_OPEN";
			break;

		}
		return stateString;
	}
	
	public static String expToString(int errCode) {
		String exceptionMessage;
		switch (errCode) {
		case GUOMI_ERR_SUCCESS:
			exceptionMessage = "Success";
			break;

		case GUOMI_ERR_CONNECT_FAILED:
			exceptionMessage = "Connect To Guomi Failed!";
			break;

		case GUOMI_ERR_CMD_TIMEOUT:
			exceptionMessage = "Guomi Cmd Timeout";
			break;

		default:
			exceptionMessage = "Error:" + errCode;
			break;

		}
		return exceptionMessage;
	}

	/**
	 * get SP's Version
	 * @return
	 */
	@Override
	public String getVersion(){
		if (dbg) Log.d(TAG, "getVersion ...");

		byte[] thisByte = new byte[128];
		int reval = mPosGuomi.getVersion(thisByte);
		if (reval < 0){
			Log.e(TAG, "getVersion Failed. reval = "+ reval);
			setExpValue(reval);
			return null;
		}

		int versionLen = (thisByte[0] & 0x000000FF) - 4;
		String versionStr = UtilFun.byteToString(thisByte, 8, versionLen);
		return versionStr;

	}

	public int getState(){
		byte[] thisStateBuff = new byte[64];
	    int reval = mPosGuomi.guomiGetState(thisStateBuff);
		if (reval != 0){
			return reval;
		}
		mGuomiState = (int)thisStateBuff[8];
		return mGuomiState;
	}



	/**
	 *
	 * @return
	 */
	@Override
	public int open(){
		if (dbg) Log.d(TAG, "open ...");
		byte slot = 0;
		int reval = mPosGuomi.guomiOpen(slot);
		if (reval < 0) {
			Log.e(TAG, "open failed: " + expToString(reval));
			return reval;
		}

		return 0;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public int close() {
		if (dbg) Log.d(TAG, "close ...");
		int reval = mPosGuomi.guomiClose();
		if (reval != 0) {
			return reval;
		}
		return 0;
	}

	/**
	 *
	 * @param command: the command APDU
	 * @param timeoutMs: timeout should be  < 200 000
	 * @return: the response APDU received from the card
	 */
	@Override
	public ResponseApdu transmit(CommandApdu command, int timeoutMs){
		if (dbg) Log.d(TAG, "transmit ...");
		byte[] commandBytes = command.getTransmitBytes();
		byte[] responseBytes = new byte[550];
		
		int reval = mPosGuomi.guomiApdu(commandBytes, timeoutMs, responseBytes);
		if (reval != 0) {
			Log.e(TAG,"guomiApdu() failed:"+expToString(reval));
			setExpValue(reval);
			return null;
		}
		
		int apduLen = UtilFun.bytesToInt32(responseBytes, 0) - 4;
		if (apduLen < 2){
			Log.e(TAG, "invalid apdu rsp:len="+apduLen);
			setExpValue(GUOMI_ERR_APDU);
			return null;
		}
		byte[] apduRsp = new byte[apduLen];
		System.arraycopy(responseBytes, 8, apduRsp, 0, apduLen);
		return new ResponseApdu(apduRsp);
	}

	public ResponseApdu transmit(CommandApdu command){
		return transmit(command, 10000);
	}


}
