package com.justtide.justj;

import android.util.Log;

import com.just.api.BcmDrvListener.OnJustUeventListener;
import com.just.api.PosDevice;
import com.just.api.UtilFun;
import com.justtide.aidl.IPsamReader;
import com.justtide.aidl.CommandApdu;
import com.justtide.aidl.ContactCard;
import com.justtide.aidl.ResponseApdu;

import java.nio.ByteBuffer;


public final class ImplPsamReader extends IPsamReader.Stub implements OnJustUeventListener{
	private static final String TAG = "ImplPsamReader";
	private static boolean dbg = false;

	public static final byte PSAM_SUPPORT_SLOT_MIN = 1;

	public static final byte PSAMCARD_SLOT1 = 1;
	public static final byte PSAMCARD_SLOT2 = 2;

	public static final byte PSAM_SUPPORT_SLOT_MAX = 2;


	public static final byte PSAM_SUPPORT_VCC_MIN = 1;
	public static final byte PSAM_VCC_5V = 1;
	public static final byte PSAM_VCC_3V = 2;
	public static final byte PSAM_VCC_1V8 = 3;
	public static final byte PSAM_SUPPORT_VCC_MAX = 1;

	public static final byte ICCARD_DEFAULT_SLOT = PSAMCARD_SLOT1;
	public static final byte ICCARD_DEFAULT_VCC_MOD = PSAM_VCC_5V;
	public static final boolean ICCARD_DEFAULT_EMV_MOD = true;


	/* Error code defined */
	public static final int PSAM_ERR_BASE = PosDevice.PSAM_ERR_BASE;
	public static final int PSAM_ERR_SUCCESS             = 0;
	public static final int PSAM_ERR_CONNECT_FAILED       = PSAM_ERR_BASE - 1;
	public static final int PSAM_ERR_CMD_TIMEOUT          = PSAM_ERR_BASE - 2;
	public static final int PSAM_ERR_VCCERR               = PSAM_ERR_BASE - 3;
	public static final int PSAM_ERR_SLOTERR              = PSAM_ERR_BASE - 4;
	public static final int PSAM_ERR_APDU                 = PSAM_ERR_BASE - 5;
	public static final int PSAM_ERR_PARERR               = PSAM_ERR_BASE - 6;
	public static final int PSAM_ERR_SEARCH_CARD_TIMEOUT  = PSAM_ERR_BASE - 7;
	public static final int PSAM_ERR_SEARC_CANCEL         = PSAM_ERR_BASE - 8;

	public static final int PSAM_STATE_CLOSED = 0;
	public static final int PSAM_STATE_OPEN=1;
	public static final int PSAM_STATE_READY = 2;

//	private static final int PSAM_DEFAULT_CHECK_TIMEOUT = 15000;

	PosDevice mPosIcCard = null;

	private int mExpValue = PSAM_ERR_SUCCESS;

	private int mPsamState = PSAM_STATE_CLOSED;

	private boolean mCancelFlag = false;

	/*private static class PsamReaderHolder {
		private static PsamReader psamReader = new PsamReader();
	}

	public static PsamReader getInstance() {
		return PsamReaderHolder.psamReader;
	}

	private ImplPsamReader() {
		mPosIcCard = new PosDevice(Device.DEV_ID_PSAM);
        mPosIcCard.setOnJustUeventListener(this);
        //mPosIcCard.DeviceOpen(0);
	}*/

	public ImplPsamReader(PosDevice inPosDevice){
		mPosIcCard = inPosDevice;
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
		case PSAM_ERR_SUCCESS:
			exceptionMessage = "Success";
			break;
		case PSAM_ERR_CONNECT_FAILED:
			exceptionMessage = "Psam Connect Failed";
			break;
		case PSAM_ERR_CMD_TIMEOUT:
			exceptionMessage = "Psam Cmd Timeout";
			break;
		case PSAM_ERR_VCCERR:
			exceptionMessage = "Power supply voltage error";
			break;
		case PSAM_ERR_SLOTERR:
			exceptionMessage = "Slot index error";
			break;
		case PSAM_ERR_APDU:
			exceptionMessage = "APDU error";
			break;
		case PSAM_ERR_PARERR:
			exceptionMessage = "Parameter Error";
			break;
		case PSAM_ERR_SEARCH_CARD_TIMEOUT:
			exceptionMessage = "Search Card Timeout";
			break;
		case PSAM_ERR_SEARC_CANCEL:
			exceptionMessage = "Search Card Timeout";
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
		case PSAM_STATE_CLOSED:
			stateString = "PSAM_STATE_CLOSED";
			break;

		case PSAM_STATE_OPEN:
			stateString = "PSAM_STATE_OPEN";
			break;

		case PSAM_STATE_READY:
			stateString = "PSAM_STATE_READY";
			break;
		default:
			stateString = "Undefined State:" + state;
			break;
		}
		return stateString;
	}


	private void setState(int newState){
		Log.i(TAG, "setState:" + stateToString(newState));
		if(newState < PSAM_STATE_CLOSED || newState > PSAM_STATE_READY){
			Log.e(TAG, "setState: Invalid newState[" + newState + "]");
		}
		mPsamState = newState;
	}


	/**
	 * get current iccard state, should be one of:PSAM_STATE_CLOSED/PSAM_STATE_OPEN/PSAM_STATE_POLLING/PSAM_STATE_UNACTIVATED/PSAM_STATE_READY
	 * @return
	 */
	public int getState() {
		byte[] thisStateBuff = new byte[32];
		int reval = mPosIcCard.psamGetState(thisStateBuff);
		if (reval != 0){
			return reval;
		}
		mPsamState = (int)thisStateBuff[8];//UtilFun.bytesToInt(thisStateBuff, 4);
		Log.i(TAG, "mPsamState="+mPsamState);
		return mPsamState;
	}

	/**
	 * @param slot
	 * @param emvMode
	 * @return
	 */
	@Override
	public int open(byte slot, boolean emvMode){
		if (dbg) Log.e(TAG, "open  ...");
		if ((slot < PSAM_SUPPORT_SLOT_MIN) || (slot > PSAM_SUPPORT_SLOT_MAX)) {
			Log.e(TAG, "open:" + expToString(PSAM_ERR_SLOTERR));
			setExpValue(PSAM_ERR_SLOTERR);
			return PSAM_ERR_SLOTERR;
		}
		/*
		int currState = getState();
		if (currState == PSAM_STATE_POLLING
			|| currState == PSAM_STATE_UNACTIVATED
			|| currState == PSAM_STATE_READY){
			mPosIcCard.icCardClose();
		}
		*/
		int reval = mPosIcCard.psamOpen(slot, emvMode);
		if (reval < 0) {
			Log.e(TAG, "open failed: " + expToString(reval));
			mPosIcCard.psamClose();
			//setState(PSAM_STATE_CLOSED);
			return reval;
		}
		//setState(PSAM_STATE_OPEN);

		return 0;
	}

	/**
	 *
	 * @param slot
	 *       card slot num
	 * @return
	 */
	public int open(byte slot){
		return open(slot, ICCARD_DEFAULT_EMV_MOD);
	}

	/**
	 *
	 * @return
	 */
	@Override
	public int close() {
		int reval = mPosIcCard.psamClose();
		if (reval != 0) {
			return reval;
		}
		setState(PSAM_STATE_CLOSED);
		return 0;
	}

	/**
     *
	 * @param timeoutMs
     * @return
     */

	/*
	public int check(int timeoutMs) {
		if (dbg) Log.e(TAG, "check  ...");

    	long start = System.currentTimeMillis();
		long end = 0;


		while (true) {
			int reval = mPosIcCard.psamCheck(0);
			if (reval == 0){
				return 0;
			}
			end = System.currentTimeMillis();
			if ((end - start) >= timeoutMs) {
				Log.e(TAG, "detect timeout:" + timeoutMs + "ms");
				return PSAM_ERR_SEARCH_CARD_TIMEOUT;
			}
			if (mCancelFlag == true){
				return PSAM_ERR_SEARC_CANCEL;
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

	public int check(){
		return check(PSAM_DEFAULT_CHECK_TIMEOUT);
	}
*/
	/**
	 *
	 * @return
	 */
	@Override
	public int checkStop(){
		mCancelFlag = true;
		return 0;
	}

	/**
	 * Enable a card and get its instance.
	 *
	 * @param slot
	 *            card slot index
	 * @param vccMode
	 *            card power supply voltage
	 * @param emvMode
	 *            if true, the reader communicates with the card with EMV
	 *            standard
	 */
	@Override
	public ContactCard enable(byte slot, byte vccMode, boolean emvMode) {
		if (dbg) Log.e(TAG, "enable(" + slot + "," + vccMode + "," + emvMode + ")  ...");

		byte[] atrTemp = new byte[40];
		int reval = mPosIcCard.psamActivate(vccMode, emvMode, atrTemp);
		if (reval != 0){
			Log.e(TAG, "enable3:" + expToString(reval));
			setExpValue(reval);
			return null;
		}

		int artLength = UtilFun.bytesToInt32(atrTemp, 0) - 4;
		byte[] atr = new byte[artLength];
		System.arraycopy(atrTemp, 8, atr, 0, artLength);

		ContactCard contactCard = new ContactCard(slot, vccMode, emvMode, atr);
		//contactCard.setState(ContactCard.IS_ENABLED);
		return contactCard;
	}

	/**
	 * activate iccard
	 * @param slot
	 * @param vccMode
	 * @return
	 */
	public ContactCard enable(byte slot, byte vccMode){
		return enable(slot, vccMode, ICCARD_DEFAULT_EMV_MOD);
	}

	public ContactCard enable(byte slot){
		return enable(slot, ICCARD_DEFAULT_VCC_MOD);
	}

	public ContactCard enable(){
		return enable(ICCARD_DEFAULT_SLOT);
	}

	/**
	 * deactivate current activated card.
	 * @return
	 */
	@Override
	public int disable(){
		if (dbg) Log.e(TAG, "disable  ...");
		int reval = mPosIcCard.psamDeactivate();
		if (reval != 0) {
			Log.e(TAG, "disable:"+ expToString(reval));
			return reval;
		}
		return 0;
	}


	/**
	 * Transmits the specified command APDU to the Smart Card and returns the
	 * response APDU.
	 *
	 * The ResponseAPDU returned by this method is the result after this
	 * processing has been performed.
	 *
	 * @param contactCard
	 *            Instance of the card
	 * @param command
	 *            the command APDU
	 * @return the response APDU received from the card
	 */
	@Override
	public ResponseApdu transmit(ContactCard contactCard, CommandApdu command){
		if (dbg) Log.e(TAG, "transmit  ...");
		byte[] apdu = command.getTransmitBytes();
		byte[] responseBytes = new byte[512];

		int reval = mPosIcCard.psamApdu(apdu, responseBytes);
		if (reval != 0) {
			Log.e(TAG,"transmit:psamApdu() failed:"+expToString(reval));
			//contactCard.setState(ContactCard.IS_ABSENT);
			return null;
		}
		int apduLen = UtilFun.bytesToInt32(responseBytes, 0) - 4;
		if (apduLen < 2){
			Log.e(TAG, "invalid apdu rsp:len="+apduLen);
			setExpValue(PSAM_ERR_APDU);
			return null;
		}
		byte[] apduRsp = new byte[apduLen];
		System.arraycopy(responseBytes, 8, apduRsp, 0, apduLen);
		return new ResponseApdu(apduRsp);
	}

	/**
	 *
	 * Transmits the specified command APDU to the Smart Card and returns the
	 * response APDU.
	 *
	 * The ResponseAPDU returned by this method is the result after this
	 * processing has been performed.
	 *
	 * @param contactCard
	 *            Instance of the card
	 * @param command
	 *            the command APDU
	 * @param timeoutMs
	 *            timeout(ms)
	 * @return  the response APDU received from the card
	 */
	public ResponseApdu transmit(ContactCard contactCard, CommandApdu command, int timeoutMs){
		mPosIcCard.setCmdTimeoutValue(mPosIcCard.POS_CMD_PSAM_APDU,timeoutMs);
		return transmit(contactCard,command);
	}

	public int transmit(ContactCard contactCard, ByteBuffer command, ByteBuffer response){
		ResponseApdu responseApdu = transmit(contactCard, new CommandApdu(command));
		if(responseApdu == null)
			return 0;
		byte[] binaryResponse = responseApdu.getBytes();
		response.put(binaryResponse);
		return binaryResponse.length;
	}

	public int transmit(ContactCard contactCard, byte[] command, byte[] response){
		ResponseApdu responseApdu = transmit(contactCard, new CommandApdu(command));
		if(responseApdu == null)
			return 0;
		byte[] binaryResponse = responseApdu.getBytes();
		System.arraycopy(binaryResponse, 0, response, 0, binaryResponse.length);
		return binaryResponse.length;
	}

	//===============================================================================

	@Override
	public void OnJustUevent(int what, int arg, byte[] buff){
		if (what < PosDevice.POS_CMD_PSAM_BASE || what > PosDevice.POS_CMD_PSAM_MAX){
			return;
		}
		Log.v(TAG, "OnJustUevent:" + what + ',' + arg + ",len=" + buff.length);
		switch(what){
		case PosDevice.POS_UNSOLI_PSAM_STATE_CHANGE:
			setState(arg);
			break;
		
		default:
			//Log.e(TAG, "OnJustUevent:Undefined event[" + what + "]");
			return;
		}
	}
	
	
	//===============================================================================

}
