package com.justtide.justj;

import android.os.RemoteException;
import android.util.Log;

import com.just.api.BcmDrvListener.OnJustUeventListener;
import com.just.api.PosDevice;
import com.just.api.UtilFun;
import com.justtide.aidl.IPiccReader;
import com.justtide.aidl.CommandApdu;
import com.justtide.aidl.ContactCard;
import com.justtide.aidl.ContactlessCard;
import com.justtide.aidl.ResponseApdu;

import java.nio.ByteBuffer;

public final class ImplPiccReader extends IPiccReader.Stub implements OnJustUeventListener {

	private static final String TAG = "ImplPiccReader";
	private static boolean dbg = true;

	public static final byte NFCCARD_SUPPORT_SLOT_MIN = 1;
	public static final byte NFC_CARD_SLOT_0 = 1;
	//public static final byte NFC_CARD_SLOT_1 = 2;
	public static final byte NFCCARD_SUPPORT_SLOT_MAX = 1;

	public static final byte NFCCARD_DEFAULT_SLOT = NFC_CARD_SLOT_0;

	public static final byte NFC_POLL_TYPE_NORMAL = 0x00;
	public static final byte NFC_POLL_TYPE_OTHER0 = 0x01;

	public static final int NFCCARD_POLL_DEFAULT_TIMEOUT = 15000; //15 seconds

	public static final int NFCCARD_REMOVE_TIMEOUT = 20000;

	public static final int NFCCARD_ERR_BASE = PosDevice.POS_ERR_NFCCARD_BASE;
	public static final int NFCCARD_ERR_SUCCESS            = 0;
	public static final int NFCCARD_ERR_CONNECT_FAILED     = NFCCARD_ERR_BASE - 1;
	public static final int NFCCARD_ERR_CMD_TIMEOUT        = NFCCARD_ERR_BASE - 2;
	public static final int NFCCARD_ERR_SLOTERR            = NFCCARD_ERR_BASE - 3;
	public static final int NFCCARD_ERR_APDU               = NFCCARD_ERR_BASE - 4;
	public static final int NFCCARD_ERR_USER_CANCEL        = NFCCARD_ERR_BASE - 5;
	public static final int NFCCARD_ERR_INVALID_PARAM      = NFCCARD_ERR_BASE - 6;
	public static final int NFCCARD_ERR_CARD_TYPE_ERR      = NFCCARD_ERR_BASE - 7;
	public static final int NFCCARD_ERR_INVALID_CARD       = NFCCARD_ERR_BASE - 8;
	public static final int NFCCARD_ERR_UNREMOVE_ERR       = NFCCARD_ERR_BASE - 9;
	public static final int NFCCARD_ERR_SEARCH_CARD_TIMEOUT= NFCCARD_ERR_BASE - 10;


	public static final byte NFCCARD_SEARCH_TYPE_ABMC        = 0x00;  /* 寻A、B、M、C卡*/
	public static final byte NFCCARD_SEARCH_TYPE_CNID        = 0x01;  /* 寻身份证 */

	public static final byte NFCCARD_SEARCH_TYPE_ALL        = 0x00;   //legency value will del

	/** < file name error */
	//public static final int NFC_FILE_NAME_ERR       = -9999;
	/** < file fd error */
	//public static final int NFC_FILE_FD_ERR         = -9998;
	/** < message null error */
	//public static final int NFC_MSG_NULL_ERR        = -9997;
	/** < message format error */
	//public static final int NFC_MSG_FORMAT_ERR      = -9996;
	/** < open file error */
	//public static final int NFC_OPEN_ERR            = -9995;
	/** < operate state error */
	//public static final int NFC_OPERATE_STATE_ERR   = -9994;
	/** < start poll error */
	//public static final int NFC_START_POLL_ERR      = -9993;
	/** < polling error */
	//public static final int NFC_POLLING_ERR         = -9992;
	/** < polling timeout error */
	//public static final int NFC_POLL_TIMEOUT_ERR    = -9991;
	/** < no card detected error */
	//public static final int NFC_UNDETECTED_ERR      = -9990;
	/** < card unremoved error */
	//public static final int NFC_CARD_UNREMOVED_ERR  = -9989;
	/** < exchange apdu error */
	//public static final int NFC_EXCHANGE_APDU_ERR   = -9988;


	public static final int NFC_DBG_PANIC    = (0x01 << 0);
	public static final int  NFC_DBG_ERROR   = (0x01 << 1);
	public static final int  NFC_DBG_WARN    = (0x01 << 2);
	public static final int  NFC_DBG_NOTICE  = (0x01 << 3);
	public static final int  NFC_DBG_INFO    = (0x01 << 4);
	public static final int  NFC_DBG_RTX     = (0x01 << 5);
	public static final int  NFC_DBG_REG     = (0x01 << 6);

	public static final int NFCCARD_STATE_CLOSED   = 0;
	public static final int NFCCARD_STATE_OPEN     = 1;
	public static final int NFCCARD_STATE_POLLING  = 2;
	public static final int NFCCARD_STATE_READY    = 3;
	public static final int NFCCARD_STATE_REMOVING = 4;

	public static final int NFCCARD_SWIPE_MAG_NUMA = 9527;
	public static final int NFCCARD_SWIPE_MAG_NUMB = 9528;
	public static final int NFCCARD_REMOVE_MAG_NUMA = 9527;
	public static final int NFCCARD_REMOVE_MAG_NUMB = 9528;

	public boolean isCancel = false;

	PosDevice mPosNfcCard = null;

	public int mNfcState = NFCCARD_STATE_CLOSED;

	public int mNfcSwipeFlag = NFCCARD_SWIPE_MAG_NUMA;

	public int mNfcRemoveFlag = NFCCARD_REMOVE_MAG_NUMA;

	private int mExpValue = NFCCARD_ERR_SUCCESS;

	/*private static class PiccReaderHolder {
		private static PiccReader piccReader = new PiccReader();
	}

	public static PiccReader getInstance() {
		return PiccReaderHolder.piccReader;
	}

	private ImplPiccReader() {
		mPosNfcCard = new PosDevice(Device.DEV_ID_NFC_CARD);
		mPosNfcCard.setOnJustUeventListener(this);
		//mPosNfcCard.DeviceOpen(0);
	}*/

	public ImplPiccReader(PosDevice inPosDevice){
		mPosNfcCard = inPosDevice;
	}
	/**
	 * 设置是否打开模块日志
	 * @param flag  true:打开，false：关闭
	 */
	public void logOpen(boolean flag){
		dbg = false;
	}

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
		case NFCCARD_STATE_CLOSED:
			stateString = "NFCCARD_STATE_CLOSED";
			break;
			
		case NFCCARD_STATE_OPEN:
			stateString = "NFCCARD_STATE_OPEN";
			break;
			
		case NFCCARD_STATE_POLLING:
			stateString = "NFCCARD_STATE_POLLING";
			break;
			
		case NFCCARD_STATE_READY:
			stateString = "NFCCARD_STATE_READY";
			break;
			
		case NFCCARD_STATE_REMOVING:
			stateString = "NFCCARD_STATE_REMOVING";
			break;
		}
		return stateString;
	}
	
	public static String expToString(int errCode) {
		String exceptionMessage;
		switch (errCode) {
		case NFCCARD_ERR_SUCCESS:
			exceptionMessage = "Success";
			break;
		case NFCCARD_ERR_CONNECT_FAILED:
			exceptionMessage = "Connect To NfcCard Failed!";
			break;
		case NFCCARD_ERR_CMD_TIMEOUT:
			exceptionMessage = "Nfc Cmd Timeout";
			break;
		case NFCCARD_ERR_SLOTERR:
			exceptionMessage = "Slot Error!";
			break;
		case NFCCARD_ERR_USER_CANCEL:
			exceptionMessage = "Canceled";
			break;
		case NFCCARD_ERR_APDU:
			exceptionMessage = "Apdu Error";
			break;
		case NFCCARD_ERR_INVALID_PARAM:
			exceptionMessage = "Invalid Parameter";
			break;
		case NFCCARD_ERR_CARD_TYPE_ERR:
			exceptionMessage = "Not Support Cardtype";
			break;
		case NFCCARD_ERR_INVALID_CARD:
			exceptionMessage = "Invalid Card";
			break;
		case NFCCARD_ERR_SEARCH_CARD_TIMEOUT:
			exceptionMessage = "Search Card Timeout";
			break;
		case NFCCARD_ERR_UNREMOVE_ERR:
			exceptionMessage = "Unremove Error";
			break;
		default:
			exceptionMessage = "Error:" + errCode;
			break;

		}
		return exceptionMessage;
	}
	
	private void setState(int newState){
		Log.i(TAG, "setState:" + stateToString(newState));
		if(newState < NFCCARD_STATE_CLOSED || newState > NFCCARD_STATE_REMOVING){
			Log.e(TAG, "setState: Ilvalid newState[" + newState + "]");
		}
		mNfcState = newState;
	}

	public int getState(){
		byte[] thisStateBuff = new byte[64];
	    int reval = mPosNfcCard.nfcCardGetState(thisStateBuff);
		if (reval != 0){
			return reval;
		}
		mNfcState = (int)thisStateBuff[8];
		return mNfcState;
	}
	
	
	private void clearSwipFlag(){
		mNfcSwipeFlag = NFCCARD_SWIPE_MAG_NUMA;
	}

	@Override
	public int open(byte slot){
		if (dbg) Log.d(TAG, "open ...");
		if ((slot < NFCCARD_SUPPORT_SLOT_MIN) || (slot > NFCCARD_SUPPORT_SLOT_MAX)) {
			Log.e(TAG, "open:" + expToString(NFCCARD_ERR_SLOTERR));
			return NFCCARD_ERR_SLOTERR;
		}

		int reval = mPosNfcCard.nfcCardOpen(slot);
		if (reval < 0) {
			Log.e(TAG, "open failed: " + expToString(reval));
			mPosNfcCard.nfcCardClose();
			setState(NFCCARD_STATE_CLOSED);
			return reval;
		}
		setState(NFCCARD_STATE_OPEN);
		
		return 0;
	}
	
	public int open(){
		return open(NFCCARD_DEFAULT_SLOT);
	}

	@Override
	public int close() {
		if (dbg) Log.d(TAG, "close ...");
		int reval = mPosNfcCard.nfcCardClose();
		if (reval != 0) {
			return reval;
		}
		setState(NFCCARD_STATE_CLOSED);
		return 0;
	}

	/**
	 * Search a nfc card.
	 * pollMode: should be NFC_POLL_TYPE_NORMAL without any custom order
	 * cardType: should be one of: NFCCARD_SEARCH_TYPE_ALL/NFCCARD_SEARCH_TYPE_A/。。。
	 */
	@Override
	public int search(byte pollMode, byte cardType, int timeoutMs){
		if (dbg) Log.d(TAG, "search ...[pollMode=" + pollMode + ",cardType=" + cardType + ",timeoutMs=" + timeoutMs + "]");
		int reval = 0;
		setExpValue(0);
		reval = mPosNfcCard.nfcCardPoll(pollMode, cardType, timeoutMs);
		if (reval != 0) {
			return reval;
		}
		clearSwipFlag();
		return 0;
	}
	
	public int search(byte cardType, int timeouMs) {
		return search(NFC_POLL_TYPE_NORMAL, cardType, timeouMs);
	}
	
	public int search(int timeouMs){
		return search(NFCCARD_SEARCH_TYPE_ABMC, timeouMs);
	}
	
	public int search(){
		return search(NFCCARD_POLL_DEFAULT_TIMEOUT);
	}

	/*
	public int detect(PiccInterface piccInterface){
		int reval = 0;
		
		if (mNfcSwipeFlag == NFCCARD_SWIPE_MAG_NUMA){
			return 1;
		}
		
		if(mNfcState != NFCCARD_STATE_READY){
			return getExpValue();
		}
		byte[] thisStateBuff = new byte[64];
		reval = mPosNfcCard.nfcCardGetState(thisStateBuff);
		if (reval != 0){
			return reval;
		}
		
		ContactlessCard contactlessCard = new ContactlessCard(thisStateBuff, 9);
		//piccInterface.getContactlessCard(reval, contactlessCard);
		
		return 0;
	}
	*/
	@Override
	public ContactlessCard detect() throws RemoteException {
		int reval = 0;

		if (mNfcSwipeFlag == NFCCARD_SWIPE_MAG_NUMA){
			return null;
		}

		if(mNfcState != NFCCARD_STATE_READY){
			return null;
		}
		byte[] thisStateBuff = new byte[64];
		reval = mPosNfcCard.nfcCardGetState(thisStateBuff);
		if (reval != 0){
			setExpValue(reval);
			return null;
		}

		return new ContactlessCard(thisStateBuff, 9);
	}


	public int checkCardType(int cardType){
		if (dbg) Log.d(TAG, "check ...");
		if (cardType != ContactlessCard.NFCCARD_TYPE_A
		    	&& cardType != ContactlessCard.NFCCARD_TYPE_B
				&& cardType != ContactlessCard.NFCCARD_TYPE_M50
				&& cardType != ContactlessCard.NFCCARD_TYPE_M70
				&& cardType != ContactlessCard.NFCCARD_TYPE_FELICA
				&& cardType != ContactlessCard.NFCCARD_TYPE_CN_ID){
			return NFCCARD_ERR_CARD_TYPE_ERR;
		}
		
		return 0;
	}
	/**
	 * Stop searching a PICC card.
	 * 
	 * <p>
	 * This function is called to terminate the searching block.
	 */
	public void searchStop() {
		//isCancel = true;
		mPosNfcCard.nfcCardPollStop();
	}


	/**
	 * Transmits the specified command APDU to the Smart Card and returns the
	 * response APDU.
	 *
	 * The ResponseAPDU returned by this method is the result after this
	 * processing has been performed.
	 * 
	 * @param command
	 *            the command APDU
	 * @param exPara
	 *            extend parameter
	 * @return the response APDU received from the card
	 */
	@Override
	public ResponseApdu transmit(CommandApdu command, byte[] exPara){
		if (dbg) Log.d(TAG, "transmit ...");
		byte[] commandBytes = command.getTransmitBytes();
		byte[] responseBytes = new byte[550];
		
		int reval = mPosNfcCard.nfcCardApdu(commandBytes, exPara, responseBytes);
		if (reval != 0) {
			Log.e(TAG,"transmit:nfcCardApdu() failed:"+expToString(reval));
			setExpValue(reval);
			return null;
		}
		
		int apduLen = UtilFun.bytesToInt32(responseBytes, 0) - 4;
		if (apduLen < 2){
			Log.e(TAG, "invalid apdu rsp:len="+apduLen);
			setExpValue(NFCCARD_ERR_APDU);
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
	 * @param command
	 *            the command APDU
	 * @param exPara
	 *            extend parameter
	 * @param timeoutMs
	 *            timeout(ms)
	 * @return  the response APDU received from the card
	 */
	public ResponseApdu transmit(ContactCard contactCard, CommandApdu command, byte[] exPara, int timeoutMs){
		mPosNfcCard.setCmdTimeoutValue(mPosNfcCard.POS_CMD_PSAM_APDU,timeoutMs);
		return transmit(command, exPara);
	}

	/**
	 * Transmits the specified command APDU to the Smart Card and returns the
	 * response APDU.
	 *
	 * The ResponseAPDU returned by this method is the result after this
	 * processing has been performed.
	 *
	 * @param command
	 *            the command APDU
	 * @return the response APDU received from the card
	 */
	public ResponseApdu transmit(CommandApdu command){
		byte[] exPara = new byte[0];
		return transmit(command, exPara);
	}


	public int transmit(ByteBuffer command, ByteBuffer response) {
		ResponseApdu responseApdu = transmit(new CommandApdu(command));
		if(responseApdu == null)
			return 0;
		byte[] binaryResponse = responseApdu.getBytes();
		response.put(binaryResponse);
		return binaryResponse.length;
	}

	public int transmit(byte[] command, byte[] response) {
		ResponseApdu responseApdu = transmit(new CommandApdu(command));
		if(responseApdu == null)
			return 0;
		byte[] binaryResponse = responseApdu.getBytes();
		System.arraycopy(binaryResponse, 0, response, 0, binaryResponse.length);
		return binaryResponse.length;
	}

	/**
	 * Block to wait the card removed from the sense region
	 * 
	 */
	@Override
	public int remove() {
		Log.e(TAG, "remove ...");
		
		int reval = mPosNfcCard.nfcCardRemove();
		if (reval != 0) {
			Log.e(TAG, "remove failed:" + expToString(reval));
			return reval;
		}
		mNfcRemoveFlag = NFCCARD_REMOVE_MAG_NUMA;
		return 0;
	}

	@Override
	public boolean checkIfRemoved() {
		return mNfcRemoveFlag != NFCCARD_REMOVE_MAG_NUMA;
	}

	/**
	 *   M1 authentication
	 * @param blockNumber   the block number that going to authenticate
	 *                      for S50 Card, this value should be 0 to 63,
	 *                      for S70 Card, this value should be 0 to 255
	 * @param keyType   the key type that going to authenticate
	 *                  'A' : to authenticate A key
	 *                  'B' : to authenticate B Key
	 * @param key    key data, 6 bytes
	 * @return
	 */
	@Override
	public int m1Authentication(byte blockNumber, byte keyType, byte[] key) {
		Log.i(TAG, "m1Authentication(" + blockNumber + "," + keyType + ") ...");

		int reval = 0;
		if (key == null){
			Log.e(TAG, "Invalid param key == null or uid == null" );
			return NFCCARD_ERR_INVALID_PARAM;
		}
		if (key.length != 6){
			Log.e(TAG, "Invalid param: key.length=" + key.length );
			return NFCCARD_ERR_INVALID_PARAM;
		}
		reval = mPosNfcCard.nfcCardM1Authentication(blockNumber, keyType, key);
		if (reval < 0){
			Log.e(TAG, "nfcCardM1Authentication failed:" + expToString(reval));
			return reval;
		}
		return 0;
	}

	/**
	 *
	 * @param blockNumber  the block number that going to authenticate
	 *                      for S50 Card, this value should be 0 to 63,
	 *                      for S70 Card, this value should be 0 to 255
	 * @return  byte[] :the data that read from the card
	 */
	@Override
	public byte[] m1BlockDataRead(byte blockNumber) {
		Log.i(TAG, "m1BlockDataRead(" + blockNumber + ")...");
		byte[] thisByte = new byte[72];

		int reval = 0;

		reval = mPosNfcCard.nfcCardM1BlockDataRead(blockNumber, thisByte);
		if (reval < 0){
			setExpValue(reval);
			Log.e(TAG, "nfcCardM1Authentication failed:" + expToString(reval));
			return null;
		}
		int blockDataLen = UtilFun.bytesToInt32(thisByte, 0) - 4;
		byte[] outBlockData = new byte[blockDataLen];
		System.arraycopy(thisByte, 8, outBlockData, 0, blockDataLen);
		return outBlockData;
	}

	/**
	 *
	 * @param blockNumber   he block number that going to authenticate
	 *                      for S50 Card, this value should be 0 to 63,
	 *                      for S70 Card, this value should be 0 to 255
	 * @param inData   the data to write, 16bytes
	 * @return
	 */
	@Override
	public int m1BlockDataWrite(byte blockNumber, byte[] inData) {
		Log.i(TAG, "m1Authentication(" + blockNumber + ") ...");
		int reval = 0;
		reval = mPosNfcCard.nfcCardM1BlockDataWrite(blockNumber, inData);
		if (reval < 0){
			Log.e(TAG, "nfcCardM1BlockDataWrite failed:" + expToString(reval));
			return reval;
		}
		return 0;
	}

	/**
	 *
	 * @param blockNumber  he block number that going to authenticate
	 *                      for S50 Card, this value should be 0 to 63,
	 *                      for S70 Card, this value should be 0 to 255
	 * @param value    initialize value that write in the card for this blockNumber
	 * @return
	 */
	@Override
	public int m1ValueDataSet(byte blockNumber, int value) {
		Log.i(TAG, "m1ValueDataSet ( " + blockNumber + "," + value + ")...");
		int reval = 0;

		reval = mPosNfcCard.nfcCardM1ValueDataSet(blockNumber, value);
		if (reval < 0){
			Log.e(TAG, "nfcCardM1ValueDataSet failed:" + expToString(reval));
			return reval;
		}
		return 0;
	}

	/**
	 *
	 * @param blockNumber  he block number that going to authenticate
	 *                      for S50 Card, this value should be 0 to 63,
	 *                      for S70 Card, this value should be 0 to 255
	 * @return
	 */
	@Override
	public int m1ValueDataRead(byte blockNumber, int[] outValue) {
		Log.i(TAG, "m1ValueDataRead(" + blockNumber + ") ...");
		byte[] thisByte = new byte[72];
		int reval = 0;

		if (outValue == null || outValue.length < 1){
			Log.e(TAG, "invalid param in m1ValueDataRead: outValue can not be null");
			return NFCCARD_ERR_INVALID_PARAM;
		}
		reval = mPosNfcCard.nfcCardM1ValueDataRead(blockNumber, thisByte);
		if (reval < 0){
			Log.e(TAG, "nfcCardM1ValueDataRead failed:" + expToString(reval));
			return reval;
		}
		outValue[0] = UtilFun.bytesToInt32(thisByte,8);

		return 0;
	}

	/**
	 *
	 * @param blockNumber   he block number that going to authenticate
	 *                      for S50 Card, this value should be 0 to 63,
	 *                      for S70 Card, this value should be 0 to 255
	 * @param value
	 * @return
	 */
	@Override
	public int m1ValueDataAdd(byte blockNumber, int value) {
		Log.i(TAG, "m1ValueDataAdd(" + blockNumber + ") ...");
		int reval = 0;
		reval = mPosNfcCard.nfcCardM1ValueDataAdd(blockNumber, value);
		if (reval < 0){
			Log.e(TAG, "nfcCardM1ValueDataAdd failed:" + expToString(reval));
			return reval;
		}
		return 0;
	}

	/**
	 *
	 * @param blockNumber   he block number that going to authenticate
	 *                      for S50 Card, this value should be 0 to 63,
	 *                      for S70 Card, this value should be 0 to 255
	 * @param value
	 * @return
	 */
	@Override
	public int m1ValueDataDel(byte blockNumber, int value) {
		Log.i(TAG, "m1ValueDataDel(" + blockNumber + "," + value + ") ...");
		int reval = 0;
		reval = mPosNfcCard.nfcCardM1ValueDataDel(blockNumber, value);
		if (reval < 0){
			Log.e(TAG, "nfcCardM1ValueDataDel failed:" + expToString(reval));
			return reval;
		}
		return 0;
	}

	/**
	 *
	 * @param blockNumber    he block number that going to authenticate
	 *                      for S50 Card, this value should be 0 to 63,
	 *                      for S70 Card, this value should be 0 to 255
	 * @return
	 */
	@Override
	public int m1ValueDataSaveOpRet(byte blockNumber) {
		Log.i(TAG, "m1ValueDataSaveOpRet(" + blockNumber + ") ...");
		int reval = 0;
		reval = mPosNfcCard.nfcCardM1ValueDataSaveOpRet(blockNumber);
		if (reval < 0){
			Log.e(TAG, "nfcCardM1ValueDataSaveOpRet failed:" + expToString(reval));
			return reval;
		}
		return 0;
	}


	/**
	 *
	 * @param blockNumber    he block number that going to authenticate
	 *                      for S50 Card, this value should be 0 to 63,
	 *                      for S70 Card, this value should be 0 to 255
	 * @return
	 */
	@Override
	public int m1ValueDataUnloading(byte blockNumber) {
		Log.i(TAG, "m1ValueDataUnloading(" + blockNumber + ") ...");
		int reval = 0;
		reval = mPosNfcCard.nfcCardM1ValueDataUnloading(blockNumber);
		if (reval < 0){
			Log.e(TAG, "nfcCardM1ValueDataUnloading failed:" + expToString(reval));
			return reval;
		}
		return 0;
	}


	/**
	 * get the config of nfc (internal cmd)
	 * @return
	 */
	@Override
	public byte[] getConfig(){
		Log.i(TAG, "getConf() ...");
		int reval = 0;

		byte[] thisByte = new byte[72];
		reval = mPosNfcCard.nfcCardGetConfig(thisByte);
		if (reval < 0){
			Log.e(TAG, "nfcCardGetConfig failed:" + expToString(reval));
			setExpValue(reval);
			return null;
		}
		int configLen = UtilFun.bytesToInt32(thisByte,0) - 4;
		byte[] configByte = new byte[configLen];
		System.arraycopy(thisByte, 8, configByte, 0, configLen);
		return configByte;
	}

	/**
	 *
	 * @param printConf    NFC print config ,should be mixture of:
	 *                     NFC_DBG_PANIC
	 *                     NFC_DBG_ERROR
	 *                     NFC_DBG_WARN
	 *                     NFC_DBG_NOTICE
	 *                     NFC_DBG_INFO
	 *                     NFC_DBG_RTX
	 *                     NFC_DBG_REG
	 * @param abCardValidRegValue   A/B 卡valid register value, 14 bytes
	 * @return
	 */
	@Override
	public int setConfig(byte printConf, byte[] abCardValidRegValue) {
		Log.i(TAG, "getConf(" + printConf + ") ...");
		int reval = 0;
		reval = mPosNfcCard.nfcCardSetConfig(printConf, abCardValidRegValue);
		if (reval < 0){
			Log.e(TAG, "nfcCardSetConfig failed:" + expToString(reval));
			return reval;
		}
		return 0;
	}



	// ===============================================================================

	@Override
	public void OnJustUevent(int what, int arg, byte[] buff){
		if (what < PosDevice.POS_CMD_NFCCARD_BASE || what > PosDevice.POS_CMD_NFCCARD_MAX){
			return;
		}
		Log.i(TAG, "OnJustUevent:" + what + ',' + arg + ",len=" + buff.length);
		switch(what){
		case PosDevice.POS_UNSOLI_NFCCARD_STATE_CHANGE:
			Log.i(TAG, "POS_UNSOLI_NFCCARD_STATE_CHANGE");
			int newState = arg;
			int errCode = UtilFun.bytesToInt32(buff, 1);
			Log.i(TAG, "errCode="+errCode);
			setState(newState);
			setExpValue(errCode);
			if (mNfcSwipeFlag == NFCCARD_SWIPE_MAG_NUMA){
				mNfcSwipeFlag = NFCCARD_SWIPE_MAG_NUMB;
			}
			if (mNfcRemoveFlag == NFCCARD_REMOVE_MAG_NUMA){
				mNfcRemoveFlag = NFCCARD_REMOVE_MAG_NUMB;
			}

			break;

		default:
			//Log.e(TAG, "OnJustUevent:Undefined event[" + what + "]");
			return;
		}
	}

}
