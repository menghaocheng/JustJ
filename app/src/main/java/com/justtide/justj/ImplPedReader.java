package com.justtide.justj;

import android.content.Context;
import android.util.Log;

import com.just.api.BcmDrvListener.OnJustUeventListener;
import com.just.api.PosDevice;
import com.just.api.UtilFun;
import com.justtide.aidl.IPedReader;
import com.justtide.aidl.HsmObj;
import com.justtide.aidl.PedConfig;
import com.justtide.justj.PkgMap;
import com.justtide.aidl.RsaPinKey;
import com.justtide.aidl.Task;
import com.justtide.aidl.TaskType;


public final class ImplPedReader extends IPedReader.Stub implements OnJustUeventListener {

	private static final String TAG = "ImplPedReader";
	private static boolean dbg = false;

	public static final int PED_ERR_BASE = PosDevice.PED_ERR_BASE;
	public static final int PED_ERR_SUCCESS               = 0;
	public static final int PED_ERR_CONNECT_FAILED        = PED_ERR_BASE - 1;
	public static final int PED_ERR_CMD_TIMEOUT           = PED_ERR_BASE - 2;
	public static final int PED_ERR_GETPIN_TIMEOUT        = PED_ERR_BASE - 3;
	public static final int PED_ERR_SART_PINBLOCK_FAILED  = PED_ERR_BASE - 4;
	public static final int PED_ERR_FS_FORMAT_TIMEOUT     = PED_ERR_BASE - 5;
	public static final int PED_ERR_PED_FORMAT_TIMEOUT    = PED_ERR_BASE - 6;
	public static final int PED_ERR_INVAL_PARA            = PED_ERR_BASE - 7;
	public static final int PED_ERR_GETPIN_CANCEL         = PED_ERR_BASE - 8;
	public static final int PED_ERR_PKG_NO_REGISTER       = PED_ERR_BASE - 9;
	public static final int PED_ERR_GET_RANDOM_FAIL       = PED_ERR_BASE - 10;
	public static final int PED_ERR_HANDSHAKE_FAIL        = PED_ERR_BASE - 11;

	public static final int CRYPT_DES_T = 0x10;
	public static final int CRYPT_AES_T = 0x20;

	public static final int PED_FS_FORMAT_DEFAULT_TIMEOUT  = 35000;
	public static final int PED_PED_FORMAT_DEFAULT_TIMEOUT  = 10000;
	public static final int PED_TASK_WAIT_FOREVER  = -1;

	public static final int TYPE_PIN_INTERVAL		= 0x01;		/* PIN Input interval */
	public static final int TYPE_UPDATE_INTERVAL	= 0x02;		/* Firmware update interval */
	public static final int TYPE_ACCOUNT_INTERVAL	= 0x03;		/* Account data encrypt interval */

	public static final int TDEA_DECRYPT            = 0x00000000;	// TDES解密
	public static final int TDEA_ENCRYPT            = 0x00000001;	// TDES加密
	public static final int TDEA_NONE               = 0x00000002;	// 不做加解密操作(给ped的api使用)

	public static final int TDEA_MODE_ECB			= 0x00000000;	// TDEA MODE ECB
	public static final int TDEA_MODE_CBC			= 0x00000100;	// TDEA MODE CBC
	public static final int TDEA_MODE_CFB			= 0x00000200;	// TDEA MODE CFB
	public static final int TDEA_MODE_OFB			= 0x00000300;	// TDEA MODE OFB

	/* MasterKey 加密、校验 PIN、MAC */
	public static final int KEY_VERIFY_NONE			= 0x00000000;	// 无
	public static final int KEY_VERIFY_KCV			= 0x01000000;	// KCV
	public static final int KEY_VERIFY_CMAC			= 0x02000000;	// CMAC
	public static final int KEY_VERIFY_MASK			= 0xff000000;	// 掩码

	public static final int KEY_VERIFY_KVC			= KEY_VERIFY_KCV;	// KCV

	public static final int KEY_TYPE_SIEK			= 0x01;	// The key to encrypt the internal sensitive infomation(internal use)
	public static final int KEY_TYPE_MASTK			= 0x02;	// MASTER KEY
	public static final int KEY_TYPE_PINK			= 0x03;	// PIN KEY
	public static final int KEY_TYPE_MACK			= 0x04;	// MAC KEY
	public static final int KEY_TYPE_FIXPINK		= 0x05;	// Fixed PIN KEY
	public static final int KEY_TYPE_FIXMACK		= 0x06;	// Fixed MAC KEY
	public static final int KEY_TYPE_DUKPTK		    = 0x07;	// DUKPT KEY
	public static final int KEY_TYPE_EMRKEY		    = 0x08;	// The key for securty magstripe reader
	public static final int KEY_TYPE_KMMK			= 0x09;	// The key for KMM
	public static final int KEY_TYPE_EAK			= 0x0A;	// Account Data KEY
	public static final int KEY_TYPE_FIXEAK		    = 0x0B;	// Fixed Account Data KEY
	public static final int KEY_TYPE_FIXTSK		    = 0x0C;	// Fixed TSK
	//public static final int KEY_TYPE_PUK			= 0x0D;	// rsa bublic key
	//public static final int KEY_TYPE_PVK			= 0x0E;	// rsa private key
	public static final int KEY_TYPE_SNK 			= 0x0F;	// sn key
	public static final int KEY_TYPE_DESK 			= 0x10;	// des key
	public static final int KEY_TYPE_EXPIRED_KEY	= 0xFE;	// The expired key
	public static final int KEY_TYPE_INVALID		= 0xFF;	// Invalid key


	public static final int PIN_BLOCK_FORMAT_0		= 0x00;	//  PIN BLOCK FORMAT 0
	public static final int PIN_BLOCK_FORMAT_1		= 0x01;	//  PIN BLOCK FORMAT 1
	public static final int PIN_BLOCK_FORMAT_2		= 0x02;	//  PIN BLOCK FORMAT 2
	public static final int PIN_BLOCK_FORMAT_3		= 0x03;	//  PIN BLOCK FORMAT 3
	public static final int PIN_BLOCK_FORMAT_4		= 0x04;	//  PIN BLOCK FORMAT 4
	public static final int PIN_BLOCK_FORMAT_EPS	= 0x0A;	//  PIN BLOCK FORMAT EPS
	public static final int PIN_BLOCK_FORMAT_GUOMI	= 0x0F;	//  PIN BLOCK FORMAT EPS

	public static final int MAC_MODE_1				= 0x00;	//  MAC method 1, TDES-TDES...TDES
	public static final int MAC_MODE_2				= 0x01;	//  MAC method 2, XOR...XOR...TDES
	public static final int MAC_MODE_EMV			= 0x02;	//  MAC for EMV EMV, DES-DES...TDES
	public static final int MAC_MODE_CUP			= 0x03;	//  MAC for CUP, XOR-XOR...TDES(left)-XOR-TDES...
	public static final int MAC_MODE_DUKPT			= 0x04;	//  MAC for DUKPT,Expand, XOR-XOR...TDES(left)-XOR-TDES...

	public static final int PED_RET_OK             = 0x00;	//  PED OK
	public static final int PED_RET_BASE_NO		   = -2000;
	public static final int PED_RET_LOCKED         = (PED_RET_BASE_NO -  1);	//  PED Locked
	public static final int PED_RET_ERROR          = (PED_RET_BASE_NO -  2);	//  The others error
	public static final int PED_RET_COMMERR        = (PED_RET_BASE_NO -  3);	//  Communicate with PED failed
	public static final int PED_RET_NEEDAUTH       = (PED_RET_BASE_NO -  4);	//  Need auth before use sensitive service or expired
	public static final int PED_RET_AUTHERR        = (PED_RET_BASE_NO -  5);	//  PED auth failed
	public static final int PED_RET_WEAK_KEY	   = (PED_RET_BASE_NO -  6);	//  weak length key
	public static final int PED_RET_COLLISION_KEY  = (PED_RET_BASE_NO -  7);	//  collision key
	public static final int PED_RET_KEYINDEXERR    = (PED_RET_BASE_NO -  8);	//  The index of key incorrect
	public static final int PED_RET_NOKEY          = (PED_RET_BASE_NO -  9);	//  No designated key in PED
	public static final int PED_RET_KEYFULL        = (PED_RET_BASE_NO - 10);	//  Key space is full
	public static final int PED_RET_OTHERAPPKEY    = (PED_RET_BASE_NO - 11);	//  The designated key is not belong to this APP
	public static final int PED_RET_KEYLENERR      = (PED_RET_BASE_NO - 12);	//  The key length error
	public static final int PED_RET_NOPIN          = (PED_RET_BASE_NO - 13);	//  Card holder press ENTER directly when enter PIN(no PIN)
	public static final int PED_RET_CANCEL         = (PED_RET_BASE_NO - 14);	//  Card holder press CANCEL to quit enter PIN
	public static final int PED_RET_TIMEOUT        = (PED_RET_BASE_NO - 15);	//  Timeout
	public static final int PED_RET_NEEDWAIT       = (PED_RET_BASE_NO - 16);	//  Two frequent between 2 sensitive API
	public static final int PED_RET_KEYOVERFLOW    = (PED_RET_BASE_NO - 17);	//  DUKPT KEY overflow
	public static final int PED_RET_NOCARD         = (PED_RET_BASE_NO - 18);	//  No ICC
	public static final int PED_RET_ICCNOTPWRUP    = (PED_RET_BASE_NO - 19);	//  ICC no power up
	public static final int PED_RET_PARITYERR      = (PED_RET_BASE_NO - 20);	//  The parity incorrect
	public static final int PED_RET_UNSUPPORTED	   = (PED_RET_BASE_NO - 255);	//  can not support


	//证书类型
	public static final int HSM_OBJECT_TYPE_private_key   = 0; //私钥证书
	public static final int HSM_OBJECT_TYPE_public_key    = 1;//公钥证书
	public static final int HSM_OBJECT_TYPE_cert          = 2;//CERT证书

	public static final int HSM_OBJECT_DATA_TYPE_pem      = 0;  //pem证书格式
	public static final int HSM_OBJECT_DATA_TYPE_der	  = 1;  //der编码证书格式
	public static final int HSM_OBJECT_DATA_TYPE_p7d      = 2;  //PKCS #7证书格式
	public static final int HSM_OBJECT_DATA_TYPE_pfx      = 3;  //PKCS #12证书格式

	public static final int PED_PASSWORD_A			=1;
	public static final int PED_PASSWORD_B			=2;

	//printer state
	public static final int PED_STATE_CLOSED = 0;
	public static final int PED_STATE_OPEN = 1;

	public static final int PINBLOCK_MAXLEN = 8;

	public static final int PED_FLAG_MAG_NUMA = 9527;

	public static final int PED_FLAG_MAG_NUMB = 9528;

	PosDevice mPosPed = null;

	public int mExpValue = PED_ERR_SUCCESS;

	public static int mPedState = PED_STATE_CLOSED;

	static PkgMap mPkgMap  = new PkgMap();

	PedConfig mPedConfig = null;

	public byte[] mPinBlock = null;
	public byte[] mIccOfflinePainPinBlock = null;
	public byte[] mIccOfflineCihperBlock = null;
	public int[] mExpectPinLenList = null;
	public String[] mRandom10 = null;
	public String mTitle = null;
	public String mPricStr = null;
	public long lastTriggerTime = 0;
	public volatile boolean mTaskWaitFlag = false;
	public volatile boolean mStartPinblockFlag = false;
	public volatile boolean mHidePinblockFlag = false;
	public int mStatus = 0;

	public int mGetPinFlag = PED_FLAG_MAG_NUMA;
	public int mIccOfflinePlainPinFlag = PED_FLAG_MAG_NUMA;
	public int mIccOfflineCipherFlag = PED_FLAG_MAG_NUMA;
	public int mFsFormatFlag = PED_FLAG_MAG_NUMA;
	public int mPedFormatFlag = PED_FLAG_MAG_NUMA;
	public int mPinTimeout = 60 * 1000;

	//private ITaskManager taskManager = null;
    //private TaskServiceConnection conn = null;
	//private Context mContext = null;
	public boolean mCancelFlag = false;
	static boolean mPciFlag = false;
	//static private String ALLOW_TASK_WAIT_PKG = "com.justtide.taskserver";

	/*private static class PedReaderHolder {
		private static PedReader mThermalPrinter = new PedReader();
	}

	*/
	//**
	 //*
	 //* @param conn
	 //* @param nPciFlag  set this true for pci test
	 //* @return
	 //*/
	 /*
	public static PedReader getInstance(Context conn, boolean nPciFlag) {
		Log.e(TAG, "getInstance: (" + nPciFlag + ")" );

		if(conn != null) {
			mPciFlag = nPciFlag;
			String pkgName = conn.getPackageName();
			regPackageName(pkgName);
		}
		return PedReaderHolder.mThermalPrinter;
	}

	public static PedReader getInstance(Context conn) {
		if (dbg) Log.e(TAG, "getInstance()");
		return getInstance(conn, false);
	}

	private static int regPackageName(String pkgName){
		if (dbg) Log.d(TAG, "regPackageName(" + pkgName + ")");

		if (pkgName == null){
			Log.e(TAG, "pkgName cannot be null");
			return PED_ERR_INVAL_PARA;
		}
		mPkgMap.putPkgName(pkgName);
		return 0;
	}

	private ImplPedReader() {
		mPosPed = new PosDevice(Device.DEV_ID_PED);
		if(mPosPed != null) {
			mPosPed.setOnJustUeventListener(this);
			//mPosPed.DeviceOpen(0);
		}
		//conn = new TaskServiceConnection();
	}
*/
	public ImplPedReader(PosDevice inPosDevice){
		mPosPed = inPosDevice;
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
		case PED_ERR_SUCCESS:
			exceptionMessage = "Success!";
			break;

		case PED_ERR_CONNECT_FAILED:
			exceptionMessage = "Connect To ped Failed!";
			break;

		case PED_ERR_CMD_TIMEOUT:
			exceptionMessage = "Ped Cmd Timeout";
			break;

		case PED_ERR_GETPIN_TIMEOUT:
			exceptionMessage = "Getpin Timeout";
			break;

		case PED_ERR_SART_PINBLOCK_FAILED:
			exceptionMessage = "Start PinBlock Failed";
			break;

		case PED_ERR_FS_FORMAT_TIMEOUT:
			exceptionMessage = "Fs Format Timeout";
			break;

		case PED_ERR_PED_FORMAT_TIMEOUT:
			exceptionMessage = "Ped Format Timeout";
			break;

		case PED_ERR_INVAL_PARA:
			exceptionMessage = "Invalid Parameter";
			break;
		case PED_ERR_GETPIN_CANCEL:
			exceptionMessage = "Get pin Cancel";
			break;

			case PED_ERR_PKG_NO_REGISTER:
				exceptionMessage = "Package not register";
				break;

			case PED_ERR_GET_RANDOM_FAIL:
				exceptionMessage = "Get Random Faild";
				break;

			case PED_ERR_HANDSHAKE_FAIL:
				exceptionMessage = "Handshake Failed";
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
	private static String stateToString(int state){
		String stateString = "";
		switch (state){
		case PED_STATE_CLOSED:
			stateString = "PED_STATE_CLOSED";
			break;
			
		case PED_STATE_OPEN:
			stateString = "PED_STATE_OPEN";
			break;
			
		default:
			stateString = "Undefined State:" + state;
			break;
		}
		return stateString;
	}


	private void setState(int newState){
		Log.i(TAG, "setState:" + stateToString(newState));
		if(newState < PED_STATE_CLOSED || newState > PED_STATE_OPEN){
			Log.e(TAG, "setState: Ilvalid newState[" + newState + "]");
		}
		mPedState = newState;
	}
	
	/**
	 * get current ped state
	 * @return
	 */
	public int getState() {
		byte[] thisStateBuff = new byte[32];
		int reval = mPosPed.pedGetState(thisStateBuff);
		if (reval != 0){
			return reval;
		}
		mPedState = UtilFun.bytesToInt32(thisStateBuff, 8);
		Log.i(TAG, "mPedState="+ mPedState);
		return mPedState;
	}

	@Override
	public String hello(String inStr){
		return inStr;
	}

	public int hello(){
		Log.e(TAG, "hello()");
		String thisStr = "Hello";
		byte[] thisByte = new byte[72];
		int reval = mPosPed.pedHello(thisStr.getBytes(), thisByte);
		if (reval < 0){
			Log.e(TAG, "pedHello: failed, reval=" + reval );
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return reval;
		}

		return reval;
	}

	/**
	 *
	 * @return
	 */
	public long getLastTriggerTime(){
		return lastTriggerTime;
	}

	/**
	 * get ped configuration
	 * @return
	 * 			configuration info class
	 */
	@Override
	public PedConfig getConfig() {
		if (dbg) Log.d(TAG, "getConfig ...");
		byte[] thisBytes = new byte[512];
		int reval = mPosPed.pedGetConfig(thisBytes);
		if (reval < 0){
			setExpValue(reval);
			Log.e(TAG, "pedGetConfig failed:" + expToString(reval));
			return null;
		}
		int configLen = UtilFun.bytesToInt32(thisBytes, 0);
		
		mPedConfig = new PedConfig(thisBytes, 8, configLen);
		return mPedConfig;
	}
	
	/**
	 * get random bytes
	 * @param randomLen
	 * 			should be less than 128K
	 * @return
	 */
	@Override
	public byte[] getRandom(int randomLen){
		if (dbg) Log.d(TAG,"getRandom ("+randomLen+") ...");
		byte[] thisBytes = new byte[randomLen + 8];
		int reval = mPosPed.pedGetRandom(randomLen, thisBytes);
		if (reval < 0){
			setExpValue(reval);
			Log.e(TAG, "pedGetRandom failed: " + expToString(reval));
			return null;
		}
		int gotRandomLen = UtilFun.bytesToInt32(thisBytes, 0) - 4;
		if (gotRandomLen != randomLen){
			Log.e(TAG, "Unexpected randomLen, wantRandomLen:"+randomLen+",gotRandLen:"+gotRandomLen);
			return null;
		}
		byte[] thisRandomData = new byte[randomLen];
		System.arraycopy(thisBytes, 8, thisRandomData, 0, randomLen);
		return thisRandomData;
	}

	/**
	 * 
	 * @param keyType
	 * 			should be one of: KEY_TYPE_MASTK/KEY_TYPE_PINK/KEY_TYPE_MACK/KEY_TYPE_FIXPINK/KEY_TYPE_FIXMACK/KEY_TYPE_DUKPTK
	 * @param keyIndex
	 * 			should be 0~100 for KEY_TYPE_MASTK/KEY_TYPE_PINK/KEY_TYPE_MACK/KEY_TYPE_FIXPINK/KEY_TYPE_FIXMACK,
	 * 			 0~15 for KEY_TYPE_DUKPTK
	 * @return
	 */
	@Override
	public int deleteKey(int keyType, int keyIndex){
		if (dbg) Log.d(TAG, "deleteKey(" + keyType + ","+keyIndex+")");
		int reval = confirmPkgName();
		if (reval < 0){
			return reval;
		}
		reval = mPosPed.pedDeleteKey(keyType, keyIndex);
		if (reval < 0){
			Log.e(TAG, "pedDeleteKey failed. reval="+reval);
			return reval;
		}
		return 0;
	}
	
	/**
	 * 
	 * @param sensitiveType
	 * 			TYPE_PIN_INTERVAL: left time for input pin
	 * 			other value: reserve
	 * @return
	 */
	@Override
	public int getSensitiveTime(int sensitiveType){
		if (dbg) Log.d(TAG, "getSensitiveTime(" + sensitiveType + ")");
		byte[] thisBytes = new byte[16];
		int reval = mPosPed.pedGetSensitiveTime(sensitiveType, thisBytes);
		if (reval < 0){
			Log.e(TAG, "pedGetSensitiveTime failed. reval="+reval);
			return reval;
		}
		int rc = UtilFun.bytesToInt32(thisBytes, 4);
		if (rc < 0){
			return reval;
		}
		int thisSensitiveTime = UtilFun.bytesToInt32(thisBytes, 8);
		Log.d(TAG, "thisSensitiveTime:"+thisSensitiveTime);
		return thisSensitiveTime;
	}

	/**
	 * 
	 * @param timeoutSc
	 * 			should be: 30~120, if timeoutSc=0, the real timeout will be set to 60s
	 * @return
	 */
	@Override
	public int setPinInputTimeout(int timeoutSc){
		if (dbg) Log.d(TAG, "setPinInputTimeout(" + timeoutSc + ")");
		int reval = mPosPed.pedSetPinInputTimeout(timeoutSc);
		if (reval < 0){
			Log.e(TAG, "pedSetPinInputTimeout failed. reval="+reval);
			return reval;
		}
		mPinTimeout = timeoutSc * 1000;
		return 0;
	}

	/**
	 * 
	 * @param mode
	 *  		should be one of: TDEA_DECRYPT/TDEA_ENCRYPT/TDEA_NONE
	 * @param mastKeyId
	 * 			should be 0 ~ 99
	 * @param destKeyId
	 * 			should be 0 ~ 99
	 * @param keyData
	 * 			including pin key and crc data
	 * @return
	 */
	@Override
	public int writePinKey(int mode, int mastKeyId, int destKeyId,byte[] keyData){
		if (dbg) Log.d(TAG, "writePinKey(" + mode+","+mastKeyId+","+destKeyId+")");
		int reval = confirmPkgName();
		if (reval < 0){
			return reval;
		}
		reval = mPosPed.pedWritePinKey(mode,mastKeyId,destKeyId, keyData);
		if (reval < 0){
			Log.e(TAG, "pedWritePinKey failed. reval="+reval);
			return reval;
		}
		return 0;
	}
	
	/**
	 * 
	 * @param Mode
	 *  		should be one of: TDEA_DECRYPT/TDEA_ENCRYPT/TDEA_NONE
	 * @param mastKeyId
	 * 			should be 0 ~ 99
	 * @param destKeyId
	 * 			should be 0 ~ 99
	 * @param keyData
	 * 			including pin key and crc data
	 * @return
	 */
	@Override
	public int writeMacKey(int Mode, int mastKeyId, int destKeyId,byte[] keyData){
		if (dbg) Log.d(TAG, "writeMacKey(" + Mode+","+mastKeyId+","+destKeyId+")");
		int reval = confirmPkgName();
		if (reval < 0){
			return reval;
		}
		reval = mPosPed.pedWriteMacKey(Mode,mastKeyId,destKeyId, keyData);
		if (reval < 0){
			Log.e(TAG, "pedWriteMacKey failed. reval="+reval);
			return reval;
		}
		return 0;
	}
	
	
	/**
	 * 
	 * @param mode
	 * 			should be one of: TDEA_DECRYPT/TDEA_ENCRYPT/TDEA_NONE
	 * @param mastKeyId
	 * 			should be 0 ~ 99
	 * @param destKeyId
	 * 			should be 0 ~ 99
	 * @param keyData
	 * 			including pin key and crc data
	 * @return
	 * 			
	 */
	@Override
	public int writeMasterKey(int mode, int mastKeyId, int destKeyId, byte[] keyData){
		if (dbg) Log.d(TAG, "writeMasterKey(" + mode+","+mastKeyId+","+destKeyId+")");
		int reval = confirmPkgName();
		if (reval < 0){
			return reval;
		}
		reval = mPosPed.pedWriteMasterKey(mode,mastKeyId,destKeyId, keyData);
		if (reval < 0){
			Log.e(TAG, "pedWriteMasterKey failed. reval="+reval);
			return reval;
		}
		return 0;
	}

	/**
	 * 获取随机数
	 * @param length
	 * @return length不为10，返回null
	 */

	public final byte[] getRandom10(int length){

		byte[] realKeys = new byte[length];
		int[] realKeyInt = new int[length];
		for (int i=0;i<realKeyInt.length;i++){
			realKeyInt[i] = 100;
		}
		int index = 0;

		do {
			byte[] keyBuffers = getRandom(256);
			if(keyBuffers==null){
				continue;
			}
			for (int i=0;i<keyBuffers.length;i++){
				int temp = keyBuffers[i] % 10;

				if(temp<0){
					temp = temp /-1;
				}

				if(UtilFun.contains(realKeyInt,temp)) continue;
				else{
					realKeyInt[index] = temp;
					//Log.v(TAG,"getRandom, index = " + temp);
					index++;
					if(index>9){
						break;
					}
				}
			}
		}while(index<=9);


		for (int i=0;i<realKeyInt.length;i++){
			int b = realKeyInt[i];
			realKeys[i] = (byte)realKeyInt[i];

		}

		return realKeys;
	}

	/**
	 *
	 * @param keyIndex 0 ~ 1023
	 * @param pinMode should be one of: PIN_BLOCK_FORMAT_0 / PIN_BLOCK_FORMAT_EPS
	 * @param cardNo
	 *            Format 0:
	 *	          16位移位后的卡号字符串，第5到16位有效。
	 *	          Format EPS:
	 *	          10 字节字符串：前四字节由“1234”组成，后六字节由ISN组成。
	 * @param expectPinLenList
	 *    可输入的合法密码长度字符串，应用程序把允许的密码长度全部枚举出来，并且用”,”号隔开每个长度，如允许输入4、6位密码并且允许无密码直接按确认，则该字符串应该设置为”0,4,6”。
	 * @param priceStr 金额
	 * @param title  显示在Pinblock上的标题
	 * @return
	 *   8 bytes pin block
	 */
	@Override
	public byte[] getPin(int keyIndex, int pinMode, byte[] cardNo, byte[] expectPinLenList, String priceStr,String title){
		if (dbg) Log.d(TAG, "getPin(" + keyIndex+","+pinMode+")");
		mGetPinFlag = PED_FLAG_MAG_NUMA;

		int[] thisIntArray = UtilFun.getIntArrayFromByte(expectPinLenList);
		if (thisIntArray == null){
			setExpValue(PED_ERR_INVAL_PARA);
			mGetPinFlag = PED_FLAG_MAG_NUMB;
			return null;
		}

		int reval = confirmPkgName();
		if (reval < 0){
			setExpValue(reval);
			mGetPinFlag = PED_FLAG_MAG_NUMB;
			return null;
		}

		byte[] random10 = getRandom10(10);
		if (random10 == null){
			setExpValue(PED_ERR_GET_RANDOM_FAIL);
			mGetPinFlag = PED_FLAG_MAG_NUMB;
			return null;
		}

		mExpValue = 0;
		reval = mPosPed.pedGetPin(keyIndex, pinMode,expectPinLenList,cardNo);
		if (reval < 0){
			setExpValue(reval);
			Log.e(TAG, "pedGetPin failed. reval="+reval);
			mGetPinFlag = PED_FLAG_MAG_NUMB;
			return null;
		}

		if (mExpValue != 0){
			mGetPinFlag = PED_FLAG_MAG_NUMB;
			return null;
		}

		reval = mPosPed.pedStartPinBlock(expectPinLenList, random10, title, priceStr);
		if (reval < 0){
			Log.e(TAG, "pedStartPinBlock failed: reval = " + reval );
			setExpValue(PED_ERR_SART_PINBLOCK_FAILED);
			mGetPinFlag = PED_FLAG_MAG_NUMB;
			return null;
		}

		long start = System.currentTimeMillis();
		long end = 0;

		mPinBlock = null;
		mCancelFlag = false;
		while (true) {
			end = System.currentTimeMillis();
			if ((end - start) >= (mPinTimeout + 200)) {
				Log.e(TAG, "getPin timeout:" + mPinTimeout + "ms");
				setExpValue(PED_RET_TIMEOUT);
				mGetPinFlag = PED_FLAG_MAG_NUMB;
				return null;
			}
			if (mGetPinFlag != PED_FLAG_MAG_NUMA){
				if(mPinBlock == null){
					return null;
				}
				byte[] pinBlock = new byte[mPinBlock.length];
				//this part?
				System.arraycopy(mPinBlock, 0, pinBlock, 0, mPinBlock.length);
				UtilFun.clearByte(mPinBlock);
				mPinBlock = null;
				mGetPinFlag = PED_FLAG_MAG_NUMB;
				return pinBlock;
			}

			if (mCancelFlag == true){
				setExpValue(PED_ERR_GETPIN_CANCEL);
				mGetPinFlag = PED_FLAG_MAG_NUMB;
				return null;
			}
			
			try {
	            Thread.sleep(10);
	        } catch (InterruptedException e) {  
	            e.printStackTrace();
	            Log.e(TAG, "check: InterruptedException");
	        }
		}
	}

	@Override
	public int getPinCancel(){
		Log.e(TAG, "getPinCancel:");
		int reval;
		reval = mPosPed.pedHidePinBlock();
		if (reval < 0){
			setExpValue(reval);
			Log.e(TAG, "pedIccOfflinePlainPin pedHidePinBlock. reval="+reval);
		}
		mCancelFlag = true;
		return 0;
	}

	@Override
	public boolean isGetPinIng(){
		return mGetPinFlag == PED_FLAG_MAG_NUMA;
	}

		/**
	 *
	 * @param expectPinLenList  可输入的合法密码长度字符串，应用程序把允许的密码长度全部枚举出来，并且用”,”号隔开每个长度，如允许输入4、6位密码并且允许无密码直接按确认，则该字符串应该设置为”0,4,6”。
	 * @param apduData  APDU数据，固定长度为6
	 * @param pricStr 金额
	 * @param title  显示在Pinblock上的title
	 * @return
	 */
	public byte[] iccOfflinePlainPin(byte[] expectPinLenList, byte[] apduData, String pricStr, String title){
		if (dbg) Log.d(TAG, "getPin()");

		if(apduData.length != 6){
			Log.e(TAG, "iccOfflinePlainPin: apduData.length != 6" );
			setExpValue(PED_ERR_INVAL_PARA);
			return null;
		}

		int[] thisIntArray = UtilFun.getIntArrayFromByte(expectPinLenList);
		if (thisIntArray == null){
			setExpValue(PED_ERR_INVAL_PARA);
			return null;
		}

		int reval = confirmPkgName();
		if (reval < 0){
			setExpValue(reval);
			return null;
		}

		byte[] random10 = getRandom10(10);
		if (random10 == null){
			setExpValue(PED_ERR_GET_RANDOM_FAIL);
			return null;
		}

		int feedDataLen = 276;
		byte[] thisIccData = new byte[apduData.length + feedDataLen];
		System.arraycopy(apduData, 0, thisIccData, 0, apduData.length);
		mExpValue = 0;
		reval = mPosPed.pedIccOfflinePlainPin(expectPinLenList,thisIccData);
		if (reval < 0){
			setExpValue(reval);
			Log.e(TAG, "pedIccOfflinePlainPin failed. reval="+reval);
			//mPosPed.pedStartPinBlock(expectPinLenList, random10, pricStr);
			return null;
		}

		if (mExpValue != 0){
			return null;
		}

		reval = mPosPed.pedStartPinBlock(expectPinLenList, random10,title, pricStr);
		if (reval < 0){
			Log.e(TAG, "pedStartPinBlock failed: reval = " + reval );
			setExpValue(PED_ERR_SART_PINBLOCK_FAILED);
			return null;
		}
		long start = System.currentTimeMillis();
		long end = 0;

        mIccOfflinePainPinBlock = null;
		mIccOfflinePlainPinFlag = PED_FLAG_MAG_NUMA;
		mCancelFlag = false;
		while (true) {
			end = System.currentTimeMillis();
			if ((end - start) >= (mPinTimeout + 200)) {
				Log.e(TAG, "iccOfflinePlainPin timeout:" + mPinTimeout + "ms");
				setExpValue(PED_RET_TIMEOUT);
				mIccOfflinePlainPinFlag = PED_FLAG_MAG_NUMB;
				return null;
			}
			if (mIccOfflinePlainPinFlag != PED_FLAG_MAG_NUMA){
				if(mIccOfflinePainPinBlock == null){
					return null;
				}
				byte[] rspdata = new byte[mIccOfflinePainPinBlock.length];
				//this part
				System.arraycopy(mIccOfflinePainPinBlock, 0, rspdata, 0, mIccOfflinePainPinBlock.length);
				UtilFun.clearByte(mIccOfflinePainPinBlock);
				mIccOfflinePainPinBlock = null;
				return rspdata;
			}

			if (mCancelFlag == true){
				setExpValue(PED_ERR_GETPIN_CANCEL);
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

	/**
	 *
	 * @param expectPinLenList  可输入的合法密码长度字符串，应用程序把允许的密码长度全部枚举出来，并且用”,”号隔开每个长度，如允许输入4、6位密码并且允许无密码直接按确认，则该字符串应该设置为”0,4,6”。
	 * @param apduData APDU数据，固定长度为6
	 * @param rsaPinKey rsa 密钥
	 * @param pricStr 金额
	 * @param title 显示在Pinblock上的Title
	 * @return
	 */
	@Override
	public byte[] iccOfflineCipherPin(byte[] expectPinLenList, byte[] apduData, RsaPinKey rsaPinKey, String pricStr, String title){
		if (dbg) Log.d(TAG, "getPin()");
        int cp = 0;

		int[] thisIntArray = UtilFun.getIntArrayFromByte(expectPinLenList);
		if (thisIntArray == null){
			setExpValue(PED_ERR_INVAL_PARA);
			return null;
		}

		if(apduData.length != 6){
			Log.e(TAG, "iccOfflineCipherPin: apduData.length != 6");
			setExpValue(PED_ERR_INVAL_PARA);
			return null;
		}

		int reval = confirmPkgName();
		if (reval < 0){
			setExpValue(reval);
			return null;
		}

		byte[] random10 = getRandom10(10);
		if (random10 == null){
			setExpValue(PED_ERR_GET_RANDOM_FAIL);
			return null;
		}

        byte[] rsaPinKeyByte = rsaPinKey.getBytes();
		byte[] thisIccData = new byte[apduData.length + rsaPinKeyByte.length];
		System.arraycopy(apduData, 0, thisIccData, cp, apduData.length);
        cp += apduData.length;
        System.arraycopy(rsaPinKeyByte, 0, thisIccData, cp, rsaPinKeyByte.length);

        Log.e(TAG, "iccOfflineCipherPin: thisIccData.leng=" + thisIccData.length);

		mExpValue = 0;
        reval = mPosPed.pedIccOfflineCipherPin(expectPinLenList,thisIccData);
		if (reval < 0){
			setExpValue(reval);
			Log.e(TAG, "pedIccOfflineCipherPin failed. reval="+reval);
			return null;
		}

		if (mExpValue != 0){
			return null;
		}

		reval = mPosPed.pedStartPinBlock(expectPinLenList, random10, title, pricStr);
		if (reval < 0){
			Log.e(TAG, "pedStartPinBlock failed: reval = " + reval );
			setExpValue(PED_ERR_SART_PINBLOCK_FAILED);
			return null;
		}
		long start = System.currentTimeMillis();
		long end = 0;

        mIccOfflineCihperBlock = null;
		mIccOfflineCipherFlag = PED_FLAG_MAG_NUMA;
		mCancelFlag = false;
		while (true) {
			end = System.currentTimeMillis();
			if ((end - start) >= (mPinTimeout + 200)) {
				Log.e(TAG, "pedIccOfflineCipherPin timeout:" + mPinTimeout + "ms");
				setExpValue(PED_RET_TIMEOUT);
                mIccOfflineCipherFlag = PED_FLAG_MAG_NUMB;
				return null;
			}
			if (mIccOfflineCipherFlag != PED_FLAG_MAG_NUMA){
				if(mIccOfflineCihperBlock == null){
					return null;
				}
				byte[] rspdata = new byte[mIccOfflineCihperBlock.length];
				//this part?
				System.arraycopy(mIccOfflineCihperBlock, 0, rspdata, 0, mIccOfflineCihperBlock.length);
				UtilFun.clearByte(mIccOfflineCihperBlock);
                mIccOfflineCihperBlock = null;
				return rspdata;
			}

			if (mCancelFlag == true){
				setExpValue(PED_ERR_GETPIN_CANCEL);
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

	/**
	 * 
	 * @param keyIndex
	 * 		 should be 0 ~ 99
	 * @param macMode
	 *       should be one of:MAC_MODE_1/MAC_MODE_2/MAC_MODE_EMV/MAC_MODE_CUP
	 * @param inMacData
	 * @return
	 *       8 bytes mac block
	 */
	@Override
	public byte[] getMac(int keyIndex, int macMode, byte[] inMacData){
		if (dbg) Log.d(TAG, "getMac(" + keyIndex+","+macMode+")");
		int reval = confirmPkgName();
		if (reval < 0){
			setExpValue(reval);
			return null;
		}
		byte[] thisBytes = new byte[32];
		reval = mPosPed.pedGetMac(keyIndex, macMode,inMacData,thisBytes);
		if (reval < 0){
			setExpValue(reval);
			Log.e(TAG, "pedGetMac failed. reval="+reval);
			return null;
		}
		int macBlocLen = UtilFun.bytesToInt32(thisBytes, 0) - 4;
		byte[] macBlock = new byte[macBlocLen];
		System.arraycopy(thisBytes, 8, macBlock, 0, macBlocLen);
		return macBlock;
	}


	/**
	 *
	 * @param mode
	 *  		should be one of: TDEA_DECRYPT/TDEA_ENCRYPT/TDEA_NONE
	 * @param dataKeyId
	 * 			should be 0 ~ 99
	 * @param destKeyId
	 * 			should be 0 ~ 99
	 * @param keyData
	 * 			including pin key and crc data
	 * @return
	 */
	@Override
	public int writeDataKey(int mode, int dataKeyId, int destKeyId,byte[] keyData){
		if (dbg) Log.d(TAG, "writeDataKey(" + mode+","+dataKeyId+","+destKeyId+")");
		int reval = confirmPkgName();
		if (reval < 0){
			return reval;
		}
		reval = mPosPed.pedWriteDataKey(mode,dataKeyId,destKeyId, keyData);
		if (reval < 0){
			Log.e(TAG, "writeDataKey failed. reval="+ reval);
			return reval;
		}
		return 0;
	}

	/**
	 *
	 * @param keyIndex
	 * 		 should be 0 ~ 99
	 * @param dataMode
	 *     TDEA_ENCRYPT = 0x00000001
	 *     或上：
	 *     TDEA_MODE_ECB = 0x00000000
	 *     TDEA_MODE_CBC = 0x00000100
	 * @param inData
	 * @return
	 *        dataBlock, the length of dataBlock should equal  to the length of inData
	 */
	@Override
	public byte[] dataEncrypt(int keyIndex, int dataMode, byte[] inData){
		if (dbg) Log.d(TAG, "dataEncrypt(" + keyIndex+","+ dataMode+")");
		int reval = confirmPkgName();
		if (reval < 0){
			setExpValue(reval);
			return null;
		}
		byte[] thisBytes = new byte[inData.length + 32];
		reval = mPosPed.pedDataEncrypt(keyIndex, dataMode,inData,thisBytes);
		if (reval < 0){
			setExpValue(reval);
			Log.e(TAG, "pedDataEncrypt failed. reval="+reval);
			return null;
		}
		int dataBlocLen = UtilFun.bytesToInt32(thisBytes, 0) - 4;
		byte[] dataBlock = new byte[dataBlocLen];
		System.arraycopy(thisBytes, 8, dataBlock, 0, dataBlocLen);
		return dataBlock;
	}

	/**
	 *
	 * @param snKey
	 * @return
	 */
	@Override
	public int snKey(byte[] snKey){
		if (dbg) Log.e(TAG, "snKey(" + snKey.length+" bytes)");
		return mPosPed.pedSnKey(snKey);
	}

	@Override
	public int selfKeyCheck(){
		int reval = mPosPed.pedSelfKeyCheck();
		if(reval < 0){
			Log.e(TAG, "keyCheck: failed. reval = " + reval );
			return reval;
		}
		return 0;
	}

	/**
	 *
	 * @param sn
	 * @return
	 */
	@Override
	public byte[] snEncrypt(byte[] sn){
		if (dbg) Log.d(TAG, "snEncrypt(" + sn.length+" bytes)");

		byte[] thisBytes = new byte[sn.length + 32];
		int reval = mPosPed.pedSnEncrypt(sn, thisBytes);
		if (reval < 0){
			setExpValue(reval);
			Log.e(TAG, "pedSnEncrypt failed. reval="+reval);
			return null;
		}
		int snBlocLen = UtilFun.bytesToInt32(thisBytes, 0) - 4;
		byte[] snBlock = new byte[snBlocLen];
		System.arraycopy(thisBytes, 8, snBlock, 0, snBlocLen);
		return snBlock;
	}

	/**
	 *
	 * @param hsmObj
	 * @param objectData
	 * @param nDataType,should be one of:
	 *                            HSM_OBJECT_DATA_TYPE_pem
	 *                            HSM_OBJECT_DATA_TYPE_der
	 *                            HSM_OBJECT_DATA_TYPE_p7d
	 *                            HSM_OBJECT_DATA_TYPE_pfx
	 * @return
	 */
	@Override
	public int hsmSave(HsmObj hsmObj, byte[] objectData, int nDataType){
		if (dbg) Log.i(TAG, "hsmSave(" + nDataType + ")...");
		int reval = 0;
		reval = mPosPed.pedHsmSave(hsmObj.mIndex, hsmObj.mName, hsmObj.mPassword, hsmObj.mObjectType,objectData, nDataType);
		if (reval < 0){
			Log.e(TAG, "pedHsmSave failed: " + expToString(reval));
			return reval;
		}
		return 0;
	}

	/**
	 *
	 * @param hsmObj
	 * @param pPIN reserve parameter, can be null
	 * @return
	 */
	@Override
	public int hsmDelete(HsmObj hsmObj, byte[] pPIN){
		if (dbg) Log.i(TAG, "hsmDelete()...");
		int reval = 0;

		int pinLen = 0;
		if (pPIN != null){
			pinLen = pPIN.length;
		}
		byte[] thisPIN = new byte[pinLen];
		System.arraycopy(pPIN, 0, thisPIN, 0, pinLen);
		reval = mPosPed.pedHsmDelete(hsmObj.mIndex, hsmObj.mName, hsmObj.mPassword, hsmObj.mObjectType, thisPIN);
		if (reval < 0){
			Log.e(TAG, "pedHsmDelete failed: " + expToString(reval));
			return reval;
		}
		return 0;
	}

	/**
	 *
	 * @param pPIN, reserve parameter, can be null
	 * @return
	 */
	@Override
	public int hsmDeleteAll(byte[] pPIN){
		if (dbg) Log.i(TAG, "hsmDeleteAll()...");
		int pinLen = 0;
		if (pPIN != null){
			pinLen = pPIN.length;
		}

		byte[] thisPIN = new byte[pinLen];
		System.arraycopy(pPIN, 0, thisPIN, 0, pinLen);
		int reval = mPosPed.pedHsmDeleteAll(thisPIN);
		if (reval < 0){
			Log.e(TAG, "pedHsmDeleteAll failed: " + expToString(reval));
			return reval;
		}
		return 0;
	}

	/**
	 *
	 * @param hsmObj
	 * @return
	 */
	@Override
	public int hsmQueryCount(HsmObj hsmObj){
		if (dbg) Log.i(TAG, "hsmQueryCount()...");
		int reval = 0;
		byte[] thisByte = new byte[32];
		reval = mPosPed.pedHsmQueryCount(hsmObj.mIndex, hsmObj.mName, hsmObj.mPassword, hsmObj.mObjectType,thisByte);
		if (reval < 0){
			Log.e(TAG, "pedHsmQueryCount failed: " + expToString(reval));
			return reval;
		}
		int thisCount = UtilFun.bytesToInt32(thisByte, 8);
		return thisCount;
	}

	/**
	 *
	 * @param objectType
	 * @param dataType
	 * @return
	 */
	@Override
	public byte[] hsmQueryName(int objectType, int dataType){
			if (dbg) Log.i(TAG, "hsmQueryName()...");
			int reval = 0;
			byte[] thisByte = new byte[4*1024 * 72];
			reval = mPosPed.pedHsmQueryName(objectType, dataType,thisByte);
			if (reval < 0){
				Log.e(TAG, "pedHsmQueryName failed: " + expToString(reval));
				setExpValue(reval);
				return null;
			}

			int outLen = UtilFun.bytesToInt32(thisByte, 0) - 4;
			byte[] thisOut = new byte[outLen];
			System.arraycopy(thisByte, 8, thisOut, 0, outLen);

			return thisOut;
		}

	/**
	 *
	 * @param nIndex
	 * @param hsmObj
	 * @param nDataType,should be one of:
	 *                            HSM_OBJECT_DATA_TYPE_pem
	 *                            HSM_OBJECT_DATA_TYPE_der
	 *                         HSM_OBJECT_DATA_TYPE_p7d
	 *                         HSM_OBJECT_DATA_TYPE_pfx
	 * @return
	 */
	@Override
	public byte[] hsmLoad(int nIndex, HsmObj hsmObj, int nDataType){
		if (dbg) Log.i(TAG, "hsmLoad(" + nIndex + "," + nDataType + ")...");
		byte[] thisOutByte = new byte[1024*4 + 72];
		int reval = mPosPed.pedHsmLoad(nIndex, hsmObj.mIndex, hsmObj.mName, hsmObj.mPassword, hsmObj.mObjectType, nDataType, thisOutByte);
		if (reval < 0){
			setExpValue(reval);
			Log.e(TAG, "pedHsmLoad failed: " + expToString(reval));
			return null;
		}
		//return null;
		int hsmDataLen = UtilFun.bytesToInt32(thisOutByte,0) - 4;
		byte[] outData = new byte[hsmDataLen];
		System.arraycopy(thisOutByte, 8, outData, 0, hsmDataLen);
		return outData;
	}

	@Override
	public int hsmGetFreeSpace(){
		if (dbg) Log.i(TAG, "hsmGetFreeSpace()...");
		byte[] thisOutByte = new byte[72];
		int reval = mPosPed.pedHsmGetFreeSpace(thisOutByte);
		if (reval < 0){
			Log.e(TAG, "hsmGetFreeSpace failed: " + expToString(reval));
			return reval;
		}
		//return null;
		int shmFreeSpace = UtilFun.bytesToInt32(thisOutByte,8);
		return shmFreeSpace;
	}

	/**
	 *
	 * @param desKeyId
	 * @param desKey
	 * @return
	 */
	@Override
	public int injectDesKey(int desKeyId, byte[] desKey){
		if (dbg) Log.i(TAG, "injectDesKey(" + desKeyId + ")...");
		int reval = confirmPkgName();
		if (reval < 0){
			return reval;
		}
		reval = mPosPed.pedInjectDesKey(desKeyId, desKey);
		if (reval < 0){
			Log.e(TAG, "pedInjectDesKey failed: " + expToString(reval));
			return reval;
		}
		return 0;
	}

	/**
	 * calc wkey Kcv
	 * @param keyType  should be one of:
	 *                 KEY_TYPE_SIEK/KEY_TYPE_MASTK/KEY_TYPE_PINK/KEY_TYPE_MACK/KEY_TYPE_FIXPINK/KEY_TYPE_FIXMACK/KEY_TYPE_DUKPTK
	 *                 KEY_TYPE_EMRKEY/KEY_TYPE_KMMK/KEY_TYPE_EAK/KEY_TYPE_FIXEAK/KEY_TYPE_FIXTSK/KEY_TYPE_EXPIRED_KEY/KEY_TYPE_INVALID
	 *
	 * @param keyIndex
	 * @return
	 */
	@Override
	public byte[] calcWkeyKcv(int keyType, int keyIndex){
		if (dbg) Log.d(TAG, "calcWkeyKcv(" + keyType+","+ keyIndex+")");
		int reval = confirmPkgName();
		if (reval < 0){
			setExpValue(reval);
			return null;
		}
		byte[] thisBytes = new byte[72];
		reval = mPosPed.pedCalcWkeyKcv(keyType, keyIndex,thisBytes);
		if (reval < 0){
			setExpValue(reval);
			Log.e(TAG, "pedCalcWkeyKcv failed. reval="+reval);
			return null;
		}
		int outDataLen = UtilFun.bytesToInt32(thisBytes, 0) - 4;
		byte[] outData = new byte[outDataLen];
		System.arraycopy(thisBytes, 8, outData, 0, outDataLen);
		return outData;
	}

	/**
	 *  对称加密
	 * @param mode  should be mixture of:
	 *              TDEA_DECRYPT | TDEA_ENCRYPT | TDEA_NONE
	 * @param keyType  should be one of:
	 *                 KEY_TYPE_SIEK/KEY_TYPE_MASTK/KEY_TYPE_PINK/KEY_TYPE_MACK/KEY_TYPE_FIXPINK/KEY_TYPE_FIXMACK/KEY_TYPE_DUKPTK
	 *                 KEY_TYPE_EMRKEY/KEY_TYPE_KMMK/KEY_TYPE_EAK/KEY_TYPE_FIXEAK/KEY_TYPE_FIXTSK/KEY_TYPE_EXPIRED_KEY/KEY_TYPE_INVALID
	 * @param keyIndex
	 * @param IV
	 * @param inBuff
	 * @return
	 */
	@Override
	public byte[] tdea(int mode, int keyType, int keyIndex, byte[] IV, byte[] inBuff){
		if (dbg) Log.d(TAG, "tdea(" + keyType+","+ keyIndex+")");
		int reval = confirmPkgName();
		if (reval < 0){
			setExpValue(reval);
			return null;
		}
		byte[] thisBytes = new byte[inBuff.length + 72];
		if (IV.length != 8){
			Log.e(TAG, "invalid IV length in tdea" + IV.length );
			setExpValue(PED_ERR_INVAL_PARA);
			return null;
		}
		reval = mPosPed.pedTdea(mode, keyType, keyIndex,IV, inBuff, thisBytes);
		if (reval < 0){
			setExpValue(reval);
			Log.e(TAG, "pedTdea failed. reval="+reval);
			return null;
		}
		int outDataLen = UtilFun.bytesToInt32(thisBytes, 0) - 4;
		byte[] outData = new byte[outDataLen];
		System.arraycopy(thisBytes, 8, outData, 0, outDataLen);
		return outData;
	}


	/**
	 *
	 * @param keyType ,should be one of
	 *                KEY_TYPE_MASTK
	 *                KEY_TYPE_MACK
	 *                KEY_TYPE_EAK
	 *                KEY_TYPE_DESK
	 * @param keyIndex
	 * @return
	 */
	@Override
	public int getKeyLen(int keyType, int keyIndex){
		if (dbg) Log.d(TAG, "getKeyLen(" + keyType+","+ keyIndex+")");
		int reval = confirmPkgName();
		if (reval < 0){
			return reval;
		}
		byte[] thisBytes = new byte[72];
		reval = mPosPed.pedGetKeyLen(keyType, keyIndex,thisBytes);
		if (reval < 0){
			Log.e(TAG, "pedGetKeyLen failed. reval="+reval);
			return reval;
		}
		int kekLen = UtilFun.bytesToInt32(thisBytes, 8);
		return kekLen;
	}

	/**
	 *   delete the keys of the pkgName
	 * @param pkgName, if this parameter == null or "", it will delete all packages' keys
	 * @return
	 */
	@Override
	public int deleteKeys(String pkgName){
		if (dbg) Log.d(TAG, "deleteKeys(" + pkgName +")");
		int reval = 0;

		if (pkgName == null){
			byte[] thisPkgName = new byte[0];
			reval = mPosPed.pedDeleteKeys(thisPkgName);
		}
		else{
			reval = mPosPed.pedDeleteKeys(pkgName.getBytes());
		}

		if (reval < 0){
			Log.e(TAG, "pedDeleteKeys failed. reval="+reval);
			return reval;
		}

		return reval;
	}

	/**
	 *
	 * @param pkgName
	 * @return
	 */
	@Override
	public int isKeyExist(String pkgName){
		if (dbg) Log.d(TAG, "isKeyExist(" + pkgName + ")");
		if (pkgName == null){
			return PED_ERR_INVAL_PARA;
		}
		byte[] thisBuff = new byte[72];
		int reval = mPosPed.pedIsKeyExist(pkgName.getBytes(), thisBuff);
		if (reval < 0){
			Log.e(TAG, "pedIsKeyExist failed. reval="+reval);
			return reval;
		}
		int outValue = UtilFun.bytesToInt32(thisBuff, 8);
		return outValue;
	}

	/**
	 *
	 * @param mode
	 *  		should be one of: TDEA_DECRYPT/TDEA_ENCRYPT/TDEA_NONE
	 * @param mastKeyId
	 * 			should be 0 ~ 99
	 * @param destKeyId
	 * 			should be 0 ~ 99
	 * @param keyData
	 * 			including pin key and crc data
	 * @return
	 */
	@Override
	public int writeAesPinKey(int mode, int mastKeyId, int destKeyId,byte[] keyData){
        if (dbg) Log.d(TAG, "writeAesPinKey(" + mode+","+mastKeyId+","+destKeyId+")");
        int reval = confirmPkgName();
        if (reval < 0){
            return reval;
        }
        reval = mPosPed.pedWriteAesPinKey(mode,mastKeyId,destKeyId, keyData);
        if (reval < 0){
            Log.e(TAG, "pedWriteAesPinKey failed. reval="+reval);
            return reval;
        }
        return 0;
    }

    /**
	 * 用master key给工作密钥进行加密及算mac，返回密文的工作密钥及mac值（4字节）。
	 * 返回的工作密钥和传入的工作密钥长度一样
	 * @param mastKey
	 * @param inKey
	 * @return
	 */
	@Override
	public byte[] calcCmacKey(byte[] mastKey, byte[] inKey){
		if (dbg) Log.d(TAG, "calcCmacKey()");
		int reval = confirmPkgName();
		if (reval < 0){
			setExpValue(reval);
			return null;
		}
		byte[] thisByte = new byte[inKey.length + 4 + 72];
		reval = mPosPed.pedCalcCmacKey(mastKey,inKey,thisByte);
		if (reval < 0){
			Log.e(TAG, "pedCalcCmacKey failed. reval="+reval);
			setExpValue(reval);
			return null;
		}
		int outLen = UtilFun.bytesToInt32(thisByte, 0) - 4;
		byte[] outByte = new byte[outLen];

		System.arraycopy(thisByte, 8, outByte, 0, outLen);
		return outByte;
	}

	public int confirmPkgName(){

		if(mPciFlag == false){
			return 0;
		}

		String thisPkgName = mPkgMap.getPkgName();

		if (thisPkgName == null){
			Log.w(TAG, "confirmPkgName: pid[" + android.os.Process.myPid() + "]has not register yet" );
			return PED_ERR_PKG_NO_REGISTER;
		}

		Log.i(TAG, "setPkgName: " + thisPkgName);
		int reval = mPosPed.pedSetPkgName(thisPkgName.getBytes());
		if (reval < 0){
			Log.e(TAG, "pedSetPkgName failed. reval=" + reval);
			return reval;
		}
		return 0;
	}

	@Override
	public int savePassword(int index, byte[] password){
		Log.e(TAG, "setPassword("+ index + ")");

		int reval = mPosPed.pedSavePassword(index, password);
		if (reval < 0){
			Log.e(TAG, "pedSavePassword failed. reval=" + reval);
			return reval;
		}
		return 0;
	}

	@Override
	public int checkPassword(int index, byte[] password){
		Log.e(TAG, "checkPassword("+ index + ")");

		int reval = mPosPed.pedCheckPassword(index, password);
		if (reval < 0){
			Log.e(TAG, "pedCheckPassword failed. reval=" + reval);
			return reval;
		}
		return 0;
	}

	@Override
	public byte[] tdeaDencrypt(int mode, byte[] password, byte[] data){
		Log.e(TAG, "tdeaDencrypt(" + mode + ")");

		int reval = confirmPkgName();
		if (reval < 0){
			setExpValue(reval);
			return null;
		}

		byte[] thisBytes = new byte[72];
		reval = mPosPed.pedTdeaDencrypt(mode, password, data, thisBytes);
		if (reval < 0){
			setExpValue(reval);
			Log.e(TAG, "tdea_encrypt failed, reval=" + reval);
			return null;
		}
		int rspLen = UtilFun.bytesToInt32(thisBytes, 0) - 4;
		byte[] rspByte = new byte[rspLen];
		System.arraycopy(thisBytes, 8, rspByte, 0, rspLen);
		return rspByte;
	}

	private int onStartPinBlock(byte[] expectPinLenList){
		Log.e(TAG, "onStartPinBlock()");

		int cp = 0;
        int totalLen = expectPinLenList.length;

		int thisExpLen = UtilFun.bytesToInt32(expectPinLenList, 0);

		byte[] thisExpByte = new byte[thisExpLen];
		cp += 4;
		System.arraycopy(expectPinLenList, cp, thisExpByte, 0, thisExpLen);
		int[] thisIntArray = UtilFun.getIntArrayFromByte(thisExpByte);
		if (thisIntArray == null){
			Log.e(TAG, "thisIntArray == null" );
			return PED_ERR_INVAL_PARA;
		}
		mExpectPinLenList = new int[thisIntArray.length];
		System.arraycopy(thisIntArray, 0, mExpectPinLenList, 0, thisIntArray.length);

		cp += thisExpLen;
		int thisRanLen = UtilFun.bytesToInt32(expectPinLenList, cp);
		cp += 4;

		byte[] thisRandom = new byte[thisRanLen];

		System.arraycopy(expectPinLenList, cp, thisRandom, 0, thisRanLen);
        cp += thisRanLen;

		mRandom10 = UtilFun.bytesToString(thisRandom);

		//title:
		if (cp >= totalLen){
			mStartPinblockFlag = true;
			return 0;
		}

		int titleLen = UtilFun.bytesToInt32(expectPinLenList, cp);
		cp += 4;
		if (cp +titleLen > totalLen){
			mStartPinblockFlag = true;
			return 0;
		}
		mTitle = UtilFun.byteToString(expectPinLenList, cp, titleLen);
		Log.e(TAG, "mTitle=" + mTitle );
		cp += titleLen;

        //priceStr:
        if (cp >= totalLen){
            mStartPinblockFlag = true;
            return 0;
        }

        int thisPricLen = UtilFun.bytesToInt32(expectPinLenList, cp);
		cp += 4;

		if (cp +thisPricLen > totalLen){
			mStartPinblockFlag = true;
			return 0;
		}
		mPricStr = UtilFun.byteToString(expectPinLenList, cp, thisPricLen);
		Log.e(TAG, "mPriceStr=" + mPricStr );

		cp += thisPricLen;
		if (cp >= totalLen){
			mStartPinblockFlag = true;
			return 0;
		}

		mStartPinblockFlag = true;

		return 0;
	}

	private void onHidePinBlock(){
		Log.e(TAG, "onHidePinBlock()");
		synchronized (this){
			mHidePinblockFlag = true;
		}
	}

	private int onStatusChange(int status){
		Log.e(TAG, "onStatusChange("+status + ")" );
		mStatus = status;
		return 0;
	}


	// ===============================================================================
	@Override
	public void OnJustUevent(int what, int arg, byte[] buff){
		if (what < PosDevice.POS_CMD_PED_BASE || what > PosDevice.POS_CMD_PED_MAX){
			return;
		}
		Log.i(TAG, "OnJustUevent:" + what + ',' + arg + ",len=" + buff.length);
		int rc = arg;
		switch(what){
			case PosDevice.POS_CMD_PED_GET_STATE:
				onStatusChange(arg);
				break;

			case PosDevice.POS_CMD_PED_GET_PIN:
				onHidePinBlock();
				if (mGetPinFlag == PED_FLAG_MAG_NUMA) {
					setExpValue(rc);
					Log.e(TAG, "OnJustUevent: rc = " + rc );

					if (rc == 0){
						int pinBlockLen = buff.length - 4;
						mPinBlock = new byte[pinBlockLen];
						System.arraycopy(buff, 4, mPinBlock, 0, pinBlockLen);
					}
					mGetPinFlag = PED_FLAG_MAG_NUMB;
				}
				break;
			case PosDevice.POS_CMD_PED_ICC_OFFLINE_PLAIN_PIN:
				onHidePinBlock();
				if (mIccOfflinePlainPinFlag == PED_FLAG_MAG_NUMA) {
					setExpValue(rc);
					if (rc == 0){
						int offlinePainPinBlockLen = buff.length - 4;
						mIccOfflinePainPinBlock = new byte[offlinePainPinBlockLen];
						System.arraycopy(buff, 4, mIccOfflinePainPinBlock, 0, offlinePainPinBlockLen);
					}
					mIccOfflinePlainPinFlag = PED_FLAG_MAG_NUMB;
				}
				break;
			case PosDevice.POS_CMD_PED_ICC_OFFLINE_CIPHER_PIN:
				onHidePinBlock();
				if (mIccOfflineCipherFlag == PED_FLAG_MAG_NUMA) {
					setExpValue(rc);
					if (rc == 0){
						int offlineCipherBlockLen = buff.length - 4;
						mIccOfflineCihperBlock = new byte[offlineCipherBlockLen];
						System.arraycopy(buff, 4, mIccOfflineCihperBlock, 0, offlineCipherBlockLen);
					}
					mIccOfflineCipherFlag = PED_FLAG_MAG_NUMB;
				}
				break;
			case PosDevice.POS_CMD_PED_FS_FORMAT:
				if (mFsFormatFlag == PED_FLAG_MAG_NUMA) {
					setExpValue(arg);
					mFsFormatFlag = PED_FLAG_MAG_NUMB;
				}
				break;

			case PosDevice.POS_CMD_PED_PED_FORMAT:
				if (mPedFormatFlag == PED_FLAG_MAG_NUMA) {
					setExpValue(arg);
					mPedFormatFlag = PED_FLAG_MAG_NUMB;
				}
				break;

			case PosDevice.POS_UNSOLI_PED_STATE_CHANGE:
				setState(arg);
				break;

			case PosDevice.POS_INTERNAL_START_PINBLOCK:
				onStartPinBlock(buff);
				break;

			case PosDevice.POS_INTERNAL_HIDE_PINBLOCK:
				onHidePinBlock();
				break;

			default:
				//Log.e(TAG, "OnDeviceStateChange:Undefined event[" + what + "]");
				break;
		}
		
	}


	// ===============================================================================
    // internal api begin
	// ===============================================================================

	/**
	 *  将AP时间同步到SP
	 * @return
	 */
	@Override
	public int syncTime() {
		if (dbg) Log.d(TAG, "syncTime() ...");
		int reval = mPosPed.pedSyncTime();
		if (reval < 0){
			Log.e(TAG, "pedSyncTime failed" + expToString(reval));
			return reval;
		}
		return 0;
	}

	/**
	 * import the key that user input
	 * @param endKey
	 * @param inPinData
	 * @return
	 */
	@Override
	public int importPin(byte endKey, byte[] inPinData){
		if (dbg) Log.d(TAG, "importPin(" + endKey+")");

		int reval = mPosPed.pedImportPin(endKey, inPinData);
		if (reval < 0) {
			Log.e(TAG, "pedPutPin failed. reval=" + reval);
			return reval;
		}
		return 0;
	}

	/**
	 *
	 * @param timeoutMs default timeout is PED_FS_FORMAT_DEFAULT_TIMEOUT
	 * @return
	 */
	@Override
	public int fsFormat(int timeoutMs){
		if (dbg) Log.d(TAG, "fsFormat(" + timeoutMs +"ms)");

		int reval = mPosPed.pedFsFormat();
		if (reval < 0){
			Log.e(TAG, "pedFsFormat failed:" + expToString(reval) );
			return reval;
		}

		long start = System.currentTimeMillis();
		long end = 0;
		mFsFormatFlag = PED_FLAG_MAG_NUMA;
		while (true) {
			end = System.currentTimeMillis();
			if ((end - start) >= timeoutMs) {
				return PED_ERR_FS_FORMAT_TIMEOUT;
			}

			if (mFsFormatFlag != PED_FLAG_MAG_NUMA){
				if (mExpValue == PED_ERR_SUCCESS){
					Log.e(TAG, "fsFormat finished");
					return 0;
				}
				else{
					Log.e(TAG, "fsFormat failed:" + expToString(mExpValue));
					return mExpValue;
				}

			}
			try {
				Thread.currentThread();
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Log.e(TAG, "fsFormat: InterruptedException");
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public int fsFormat(){
		return fsFormat(PED_FS_FORMAT_DEFAULT_TIMEOUT);
	}

	/**
	 *
	 * @param timeoutMs  default timeout is PED_PED_FORMAT_DEFAULT_TIMEOUT
	 * @return
	 */
	@Override
	public int pedFormat(int timeoutMs){
		if (dbg) Log.d(TAG, "pedPedFormat(" + timeoutMs + "ms)");
		int reval = mPosPed.pedPedFormat();
		if (reval < 0){
			Log.e(TAG, "pedPedFormat failed:" + expToString(reval) );
			return reval;
		}

		long start = System.currentTimeMillis();
		long end = 0;
		mPedFormatFlag = PED_FLAG_MAG_NUMA;
		while (true) {
			end = System.currentTimeMillis();
			if ((end - start) >= timeoutMs) {
				return PED_ERR_PED_FORMAT_TIMEOUT;
			}

			if (mPedFormatFlag != PED_FLAG_MAG_NUMA){
				if (mExpValue == PED_ERR_SUCCESS){
					Log.e(TAG, "pedPedFormat finished");
					return 0;
				}
				else{
					Log.e(TAG, "pedPedFormat failed:" + expToString(mExpValue));
					return mExpValue;
				}

			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Log.e(TAG, "pedPedFormat: InterruptedException");
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public int pedFormat(){
		return pedFormat(PED_PED_FORMAT_DEFAULT_TIMEOUT);
	}

	/**
	 * 查询安全维护是否已经激活
	 * @return
	 *    <0： 错误
	 *    0：未激活
	 *    1：已激活
	 *    其它：保留
	 */
	@Override
	public int secMaintainCheck(){
		if (dbg) Log.e(TAG, "secMaintainCheck()");
		byte[] thisByte = new byte[72];
		int reval = mPosPed.pedSecMaintainCheck(thisByte);
		if (reval < 0){
			Log.e(TAG, "pedSecMaintainCheck failed, reval = " + reval);
			return reval;
		}
		int thisState = UtilFun.bytesToInt32(thisByte, 8);
		return thisState;
	}

	/**
	 * 安全维护激活
	 * @param actPara
	 * @return
	 */
	@Override
	public int secMaintainActivate(byte[] actPara){
		if (dbg) Log.e(TAG, "secMaintainActivate()");
		if (actPara == null){
			actPara = new byte[0];
		}

		int reval = mPosPed.pedSecMainActivate(actPara);
		if (reval < 0){
			Log.e(TAG, "pedSecMainActivate failed, reval = " + reval);
			return reval;
		}
		return 0;
	}

	/**
	 * 安全维护激活
	 * @return
	 */
	public int secMaintainActivate(){
		return secMaintainActivate(null);
	}

	public Task taskWait(int timeoutMs){
		Log.e(TAG, "taskWait: (" + timeoutMs + "ms)" );
		long start = System.currentTimeMillis();
		long end = 0;
		Task thisTask = null;

		mTaskWaitFlag = true;
		while(true) {
			if(timeoutMs > 0){
				end = System.currentTimeMillis();
				if ( (end - start) >= timeoutMs ) {
					Log.e(TAG, "taskWait timeout:" + timeoutMs + "ms");
					return null;
				}
			}

			if (mStartPinblockFlag == true) {
				Log.e(TAG, "taskWait: TASK_TYPE_PIN_BLOCK" );
				try {
					if(mPricStr == null ){
						thisTask = new Task(0, TaskType.TASK_TYPE_PIN_BLOCK, mExpectPinLenList, mRandom10);
					}
					else if(mPricStr != null && mTitle == null){
						thisTask = new Task(0, TaskType.TASK_TYPE_PIN_BLOCK, mExpectPinLenList, mRandom10, mPricStr);
					}
					else{
						thisTask = new Task(0, TaskType.TASK_TYPE_PIN_BLOCK, mExpectPinLenList, mRandom10, mPricStr,mTitle);
					}
					mTitle = null;
					mPricStr = null;
					mExpectPinLenList = null;
					mRandom10 = null;
				} catch (Exception e) {
					e.printStackTrace();
				}

				synchronized (this){
					mStartPinblockFlag = false;
				}
				return thisTask;
			}
			if (mHidePinblockFlag == true) {
				Log.e(TAG, "taskWait: TASK_TYPE_HIDE_PIN_BLOCK" );
				try {
					thisTask = new Task(1, TaskType.TASK_TYPE_HIDE_PIN_BLOCK);
				} catch (Exception e) {
					e.printStackTrace();
				}
				synchronized (this){
					mHidePinblockFlag = false;
				}

				return thisTask;
			}
			if (mStatus != 0) {
				Log.e(TAG, "taskWait: TASK_TYPE_TRIGGER(" + mStatus + ")" );
				try {
					thisTask = new Task(0, TaskType.TASK_TYPE_TRIGGER, mStatus);
				} catch (Exception e) {
					e.printStackTrace();
				}
				mStatus = 0;
				return thisTask;
			}

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}


}