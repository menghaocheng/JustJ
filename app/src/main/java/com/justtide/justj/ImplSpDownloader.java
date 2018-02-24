package com.justtide.justj;

import android.util.Log;

import com.just.api.BcmDrvListener.OnJustUeventListener;
import com.just.api.PosDevice;
import com.just.api.UtilFun;
import com.justtide.aidl.ISpDownloader;

public final class ImplSpDownloader extends ISpDownloader.Stub implements OnJustUeventListener {

	private static final String TAG = "ImplSpDownloader";
	private static boolean dbg = false;

	public static final int DOWN_ERR_BASE = PosDevice.DOWN_ERR_BASE;
	public static final int DOWN_ERR_SUCCESS             = 0;
	public static final int DOWN_ERR_CONNECT_FAILED      = DOWN_ERR_BASE - 1;
	public static final int DOWN_ERR_CMD_TIMEOUT         = DOWN_ERR_BASE - 2;
	public static final int DOWN_ERR_DOWN_FAILED         = DOWN_ERR_BASE - 3;
	public static final int DOWN_ERR_UPDATE_TIMEOUT      = DOWN_ERR_BASE - 4;

	//printer state
	public static final int DOWN_STATE_CLOSED = 0;
	public static final int DOWN_STATE_OPEN = 1;


	private static final int DOWN_FLAG_MAG_NUMA = 9527;

	private static final int DOWN_FLAG_MAG_NUMB = 9528;

	private static final int DOWN_DEFAULT_TIMEOUT = 35000;

	private static int mDownFlag = 0;

	PosDevice mPosDownloader = null;

	private int mExpValue = DOWN_ERR_SUCCESS;

	public static int mSpDownState = DOWN_STATE_CLOSED;

	/*private static class SpdownHolder {
		private static SpDownloader mSpDownloader = new SpDownloader();
	}

	public static SpDownloader getInstance() {
		return SpdownHolder.mSpDownloader;
	}

	private ImplSpDownloader() {
		mPosDownloader = new PosDevice(Device.DEV_ID_DOWN);
		mPosDownloader.setOnJustUeventListener(this);
		//mPosDownloader.DeviceOpen(0);
	}*/

	public ImplSpDownloader(PosDevice inPosDevice){
		mPosDownloader = inPosDevice;
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
	@Override
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
		case DOWN_ERR_SUCCESS:
			exceptionMessage = "Down Success!";
			break;
		case DOWN_ERR_CONNECT_FAILED:
			exceptionMessage = "Connect To SP Failed!";
			break;
		case DOWN_ERR_CMD_TIMEOUT:
			exceptionMessage = "SpDown Cmd Timeout";
			break;
		case DOWN_ERR_DOWN_FAILED:
			exceptionMessage = "SP Download Failed";
			break;
		case DOWN_ERR_UPDATE_TIMEOUT:
			exceptionMessage = "SP Update Timeout";
			break;
		default:
			exceptionMessage = "Error:" + errCode;
			break;
		}
		
		return exceptionMessage;
	}
	
	/**
	 * convert state value into String
	 * @param state
	 * @return
	 */
	public static String stateToString(int state){
		String stateString = "";
		switch (state){
		case DOWN_STATE_CLOSED:
			stateString = "DOWN_STATE_CLOSED";
			break;
			
		case DOWN_STATE_OPEN:
			stateString = "DOWN_STATE_OPEN";
			break;

		default:
			stateString = "Undefined State:" + state;
			break;
		}
		return stateString;
	}

	private void setState(int newState){
		Log.i(TAG, "setState:" + stateToString(newState));
		if(newState < DOWN_STATE_CLOSED || newState > DOWN_STATE_OPEN){
			Log.e(TAG, "setState: Ilvalid newState[" + newState + "]");
		}
		mSpDownState = newState;
	}
	
	/**
	 * get SpDownloader current state
	 * @return
	 */
	public int getState() {
		byte[] thisStateBuff = new byte[32];
		int reval = mPosDownloader.downGetState(thisStateBuff);
		if (reval != 0){
			return reval;
		}
		mSpDownState = (int)thisStateBuff[8];//UtilFun.bytesToInt(thisStateBuff, 4);
		
		return mSpDownState;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public int open() {
		if (dbg) Log.d(TAG, "open ...");
		
		getState();
		
		int reval = mPosDownloader.downOpen();
		if (reval != 0) {
			Log.e(TAG, "open failed: " + expToString(reval));
			mPosDownloader.downClose();
			return reval;
		}
		setState(DOWN_STATE_OPEN);
		return 0;
	}

	/**
	 * release SpDownloader
	 */
	@Override
	public int close() {
		if (dbg) Log.d(TAG, "close ...");
		int reval = mPosDownloader.downClose();
		if (reval != 0) {
			Log.e(TAG, "close failed: " + expToString(reval));
			return reval;
		}

		setState(DOWN_STATE_CLOSED);
		return 0;
	}
	
	/**
	 * download SP image
	 * @param writeByte
	 * @return
	 */
	@Override
	public int down(byte[] writeByte){
		if (dbg) Log.d(TAG, "down (" + writeByte.length + "byte)...");
		int reval = mPosDownloader.downWrite(writeByte);
		if( reval != 0){
			Log.e(TAG, "down(byte[]) failed: " + expToString(reval));
			return reval;
		}
		return 0;
	}
	
	/**
	 * 
	 * @param timeoutMs
	 * @return
	 */
	@Override
	public int waitForUpdateFinish(int timeoutMs){
		if (dbg) Log.d(TAG, "waitForDownFinish ...");
		long start = System.currentTimeMillis();
		long end = 0;
		
		mDownFlag = DOWN_FLAG_MAG_NUMA;
		while (true) {
			end = System.currentTimeMillis();
			if ((end - start) >= timeoutMs) {
				Log.e(TAG, "waitForFinish timeout");
				return DOWN_ERR_UPDATE_TIMEOUT;
			}
			
			if (mDownFlag != DOWN_FLAG_MAG_NUMA){
				if (mExpValue == DOWN_ERR_SUCCESS){
					Log.e(TAG, "down seccuss");
					return 0;
				}
				else{
					Log.e(TAG, "down failed:" + expToString(mExpValue));
					return mExpValue;
				}
			}
			try {  
	            Thread.currentThread();
	            Thread.sleep(10);
	        } catch (InterruptedException e) {  
	            e.printStackTrace();
	            Log.e(TAG, "down: InterruptedException");
	        }
		}
	}
	
	public int waitForUpdateFinish(){
		return waitForUpdateFinish(DOWN_DEFAULT_TIMEOUT);
	}

	/**
	 * get SP's Version
	 * @return
	 */
	@Override
	public String getVersion(){
		if (dbg) Log.d(TAG, "getVersion ...");

		byte[] thisByte = new byte[128];
		int reval = mPosDownloader.getVersion(thisByte);
		if (reval < 0){
			Log.e(TAG, "getVersion Failed. reval = "+ reval);
			setExpValue(reval);
			return null;
		}
		
		int versionLen = (thisByte[0] & 0x000000FF) - 4;
		String versionStr = UtilFun.byteToString(thisByte, 8, versionLen);
		return versionStr;
		
	}
	

	// ===============================================================================

	@Override
	public void OnJustUevent(int what, int arg, byte[] buff) {
		if (what < PosDevice.POS_CMD_DOWN_BASE || what > PosDevice.POS_CMD_DOWN_MAX){
			return;
		}
		Log.i(TAG, "OnJustUevent:" + what + ',' + arg);
		switch(what){
		case PosDevice.POS_UNSOLI_DOWN_STATE_CHANGE:
			Log.i(TAG, "POS_UNSOLI_DOWN_STATE_CHANGE");
			int newState = arg;
			if(mDownFlag == DOWN_FLAG_MAG_NUMA){
				mDownFlag = DOWN_FLAG_MAG_NUMB;
				if (newState == 0){
					Log.i(TAG, "sp download success");
					setExpValue(DOWN_ERR_SUCCESS);
				}
				else{
					Log.e(TAG, "sp download failed");
					setExpValue(DOWN_ERR_DOWN_FAILED);
				}
			}
			setState(newState);
			break;
		default:
			//Log.e(TAG, "OnJustUevent:Undefined event[" + what + "]");
			break;
		}
		
	}

}