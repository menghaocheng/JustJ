package com.justtide.justj;

import android.util.Log;

import com.just.api.BcmDrvListener.OnJustUeventListener;
import com.just.api.PosDevice;
import com.just.api.UtilFun;
import com.justtide.aidl.IIccReader;
import com.justtide.aidl.CommandApdu;
import com.justtide.aidl.ContactCard;
import com.justtide.aidl.ResponseApdu;

import java.nio.ByteBuffer;

public final class ImplIccReader extends IIccReader.Stub implements OnJustUeventListener{
	private static final String TAG = "ImplIccReader";
	private static boolean dbg = false;

	public static final byte ICCARD_SUPPORT_SLOT_MIN = 1;
	public static final byte ICCARD_SLOT = 1;

	public static final byte PSAMCARD_SLOT1 = 2;
	public static final byte PSAMCARD_SLOT2 = 3;
	public static final byte PSAMCARD_SLOT3 = 4;
	public static final byte PSAMCARD_SLOT4 = 5;

	public static final byte ICCARD_SUPPORT_SLOT_MAX = 1;



	public static final byte ICCARD_VCC_5V = 1;
	public static final byte ICCARD_VCC_3V = 2;
	public static final byte ICCARD_VCC_1V8 = 3;


	public static final byte ICCARD_DEFAULT_SLOT = ICCARD_SLOT;
	public static final byte ICCARD_DEFAULT_VCC_MOD = ICCARD_VCC_5V;
	public static final boolean ICCARD_DEFAULT_EMV_MOD = false;


	/* Error code defined */
	public static final int ICCARD_ERR_BASE = PosDevice.POS_ERR_ICCARD_BASE;
	public static final int ICCARD_ERR_SUCCESS             = 0;
	public static final int ICCARD_ERR_CONNECT_FAILED       = ICCARD_ERR_BASE - 1;
	public static final int ICCARD_ERR_CMD_TIMEOUT          = ICCARD_ERR_BASE - 2;
	public static final int ICCARD_ERR_VCCERR               = ICCARD_ERR_BASE - 3;
	public static final int ICCARD_ERR_SLOTERR              = ICCARD_ERR_BASE - 4;
	public static final int ICCARD_ERR_APDU                 = ICCARD_ERR_BASE - 5;
	public static final int ICCARD_ERR_PARERR               = ICCARD_ERR_BASE - 6;
	public static final int ICCARD_ERR_SEARCH_CARD_TIMEOUT  = ICCARD_ERR_BASE - 7;
	public static final int ICCARD_ERR_SEARC_CANCEL         = ICCARD_ERR_BASE - 8;

	public static final int ICCARD_STATE_CLOSED = 0;
	public static final int ICCARD_STATE_OPEN=1;
	public static final int ICCARD_STATE_POLLING = 2;
	public static final int ICCARD_STATE_UNACTIVATED = 3;
	public static final int ICCARD_STATE_READY = 4;

	private static final int ICCARD_DEFAULT_CHECK_TIMEOUT = 15000;

	PosDevice mPosIcCard = null;

	private int mExpValue = ICCARD_ERR_SUCCESS;

	private int mIcCardState = ICCARD_STATE_CLOSED;

	private boolean mCancelFlag = false;

	/*
	private static class IccReaderHolder {
		private static IccReader iccReader = new IccReader();
	}

	public static IccReader getInstance() {
		return IccReaderHolder.iccReader;
	}

	private ImplIccReader() {
		mPosIcCard = new PosDevice(Device.DEV_ID_IC_CARD);
        mPosIcCard.setOnJustUeventListener(this);
        //mPosIcCard.DeviceOpen(0);
	}*/

	public ImplIccReader(PosDevice inPosDevice){
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
		case ICCARD_ERR_SUCCESS:
			exceptionMessage = "Success";
			break;
		case ICCARD_ERR_CONNECT_FAILED:
			exceptionMessage = "IcCard Connect Failed";
			break;
		case ICCARD_ERR_CMD_TIMEOUT:
			exceptionMessage = "Iccard Cmd Timeout";
			break;
		case ICCARD_ERR_VCCERR:
			exceptionMessage = "Power supply voltage error";
			break;
		case ICCARD_ERR_SLOTERR:
			exceptionMessage = "Slot index error";
			break;
		case ICCARD_ERR_APDU:
			exceptionMessage = "APDU error";
			break;
		case ICCARD_ERR_PARERR:
			exceptionMessage = "Parameter Error";
			break;
		case ICCARD_ERR_SEARCH_CARD_TIMEOUT:
			exceptionMessage = "Search Card Timeout";
			break;
		case ICCARD_ERR_SEARC_CANCEL:
			exceptionMessage = "Search Card Cancel";
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
		case ICCARD_STATE_CLOSED:
			stateString = "ICCARD_STATE_CLOSED";
			break;

		case ICCARD_STATE_OPEN:
			stateString = "ICCARD_STATE_OPEN";
			break;

		case ICCARD_STATE_POLLING:
			stateString = "ICCARD_STATE_POLLING";
			break;

		case ICCARD_STATE_UNACTIVATED:
			stateString = "ICCARD_STATE_UNACTIVATED";
			break;

		case ICCARD_STATE_READY:
			stateString = "ICCARD_STATE_READY";
			break;
		default:
			stateString = "Undefined State:" + state;
			break;
		}
		return stateString;
	}


	private void setState(int newState){
		Log.i(TAG, "setState:" + stateToString(newState));
		if(newState < ICCARD_STATE_CLOSED || newState > ICCARD_STATE_READY){
			Log.e(TAG, "setState: Invalid newState[" + newState + "]");
		}
		mIcCardState = newState;
	}


	/**
	 * get current iccard state, should be one of:ICCARD_STATE_CLOSED/ICCARD_STATE_OPEN/ICCARD_STATE_POLLING/ICCARD_STATE_UNACTIVATED/ICCARD_STATE_READY
	 * @return
	 */
	public int getState() {
		byte[] thisStateBuff = new byte[32];
		int reval = mPosIcCard.icCardGetState(thisStateBuff);
		if (reval != 0){
			return reval;
		}
		mIcCardState = (int)thisStateBuff[8];//UtilFun.bytesToInt(thisStateBuff, 4);
		Log.i(TAG, "mIcCardState="+mIcCardState);
		return mIcCardState;
	}

	/**
	 * open ic card mode
	 * @param slot
	 * @param emvMode
	 * @return
	 */
	@Override
	public int open(byte slot, boolean emvMode){
		if (dbg) Log.e(TAG, "open  ...");
		if ((slot < ICCARD_SUPPORT_SLOT_MIN) || (slot > ICCARD_SUPPORT_SLOT_MAX)) {
			Log.e(TAG, "open:" + expToString(ICCARD_ERR_SLOTERR));
			setExpValue(ICCARD_ERR_SLOTERR);
			return ICCARD_ERR_SLOTERR;
		}

		int currState = getState();
		if (currState == ICCARD_STATE_POLLING
			|| currState == ICCARD_STATE_UNACTIVATED
			|| currState == ICCARD_STATE_READY){
			mPosIcCard.icCardClose();
		}
		int reval = mPosIcCard.icCardOpen(slot, emvMode);
		if (reval < 0) {
			Log.e(TAG, "open failed: " + expToString(reval));
			mPosIcCard.icCardClose();
			setState(ICCARD_STATE_CLOSED);
			return reval;
		}
		setState(ICCARD_STATE_OPEN);

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
	public int open(){
		return open(ICCARD_DEFAULT_SLOT);
	}

	/**
	 *
	 * @return
	 */
	@Override
	public int close() {
		int reval = mPosIcCard.icCardClose();
		if (reval != 0) {
			return reval;
		}
		setState(ICCARD_STATE_CLOSED);
		return 0;
	}

	/**
     *
	 * @param timeoutMs
     * @return
     */
	@Override
	public int check(int timeoutMs) {
		if (dbg) Log.e(TAG, "check("+timeoutMs +") ...");

    	long start = System.currentTimeMillis();
		long end = 0;

		mCancelFlag = false;
		while (true) {
			int reval = mPosIcCard.icCardCheck(0);
			if (reval == 0){
				return 0;
			}
			end = System.currentTimeMillis();
			if ((end - start) >= timeoutMs) {
				Log.e(TAG, "detect timeout:" + timeoutMs + "ms");
				return ICCARD_ERR_SEARCH_CARD_TIMEOUT;
			}

			if (mCancelFlag == true){
				return ICCARD_ERR_SEARC_CANCEL;
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
		return check(ICCARD_DEFAULT_CHECK_TIMEOUT);
	}

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
		int reval = mPosIcCard.icCardActivate(vccMode, emvMode, atrTemp);
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
	public int disable(){
		if (dbg) Log.e(TAG, "disable  ...");
		int reval = mPosIcCard.icCardDeactivate();
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

		int reval = mPosIcCard.icCardApdu(apdu, responseBytes);
		if (reval != 0) {
			Log.e(TAG,"transmit:icCardApdu() failed:"+expToString(reval));
			//contactCard.setState(ContactCard.IS_ABSENT);
			return null;
		}
		//int apduLen = PosDevice.getDataBuffLen(responseBytes) - 4;
		int apduLen = UtilFun.bytesToInt32(responseBytes, 0) - 4;
		if (apduLen < 2){
			Log.e(TAG, "invalid apdu rsp:len="+apduLen);
			setExpValue(ICCARD_ERR_APDU);
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
		mPosIcCard.setCmdTimeoutValue(mPosIcCard.POS_CMD_ICCARD_APDU,timeoutMs);
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
		if (what < PosDevice.POS_CMD_ICCARD_BASE || what > PosDevice.POS_CMD_ICCARD_MAX){
			return;
		}
		Log.v(TAG, "OnJustUevent:" + what + ',' + arg + ",len=" + buff.length);
		switch(what){
		case PosDevice.POS_UNSOLI_ICCARD_STATE_CHANGE:
			Log.i(TAG, "POS_UNSOLI_ICCARD_STATE_CHANGE");
			setState(arg);
			break;
		
		default:
			//Log.e(TAG, "OnJustUevent:Undefined event[" + what + "]");
			return;
		}
	}
	
	
	//===============================================================================

}
