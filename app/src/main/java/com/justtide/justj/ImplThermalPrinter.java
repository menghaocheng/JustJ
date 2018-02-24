package com.justtide.justj;

import android.graphics.Bitmap;
import android.util.Log;

import com.just.api.BcmDrvListener.OnJustUeventListener;
import com.just.api.PosDevice;
import com.just.api.UtilFun;
import com.justtide.aidl.IThermalPrinter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public final class ImplThermalPrinter extends IThermalPrinter.Stub implements OnJustUeventListener {

	private static final String TAG = "ImplThermalPrinter";
	private static boolean dbg = true;

	public static final int PRINTER_ERR_BASE = PosDevice.POS_ERR_PRINTER_BASE;
	public static final int PRINTER_ERR_SUCCESS            = 0;
	public static final int PRINTER_ERR_CONNECT_FAILED     = PRINTER_ERR_BASE - 1;
	public static final int PRINTER_ERR_CMD_TIMEOUT        = PRINTER_ERR_BASE - 2;
	public static final int PRINTER_ERR_NO_PAPER           = PRINTER_ERR_BASE - 3;
	public static final int PRINTER_ERR_OVER_TEMPERATURE   = PRINTER_ERR_BASE - 4;
	public static final int PRINTER_ERR_OVER_VOLTAGE       = PRINTER_ERR_BASE - 5;
	public static final int PRINTER_ERR_USER_CANCEL        = PRINTER_ERR_BASE - 6;
	public static final int PRINTER_ERR_INVALID_DATA       = PRINTER_ERR_BASE - 7;
	public static final int PRINTER_ERR_LOW_POWER          = PRINTER_ERR_BASE - 8;
	public static final int PRINTER_ERR_BLOCKING           = PRINTER_ERR_BASE - 9;
	public static final int PRINTER_ERR_PRINT_TIMEOUT      = PRINTER_ERR_BASE - 10;


	//printer state
	public static final int PRINTER_STATE_IDLE = 0;
	public static final int PRINTER_STATE_OPEN = 1;
	public static final int PRINTER_STATE_EXP = 2;


	//Flag
	private static final int PRINT_FLAG_MAG_NUMA = 9527;

	private static final int PRINT_FLAG_MAG_NUMB = 9528;

	private static final int PRINT_DEFAULT_TIMEOUT = 10000;


	private static final int PRINT_ONE_PACK_MAXLEN = (8*1024);

	private static int mPrintFlag = 0;

	PosDevice mPosPrinter = null;

	private int mExpValue = PRINTER_ERR_SUCCESS;

	public static int mPrinterState = PRINTER_STATE_IDLE;

	/*private static class PiccReaderHolder {
		private static ThermalPrinter mThermalPrinter = new ThermalPrinter();
	}

	public static ThermalPrinter getInstance() {
		return PiccReaderHolder.mThermalPrinter;
	}

	private ImplThermalPrinter() {
		mPosPrinter = new PosDevice(Device.DEV_ID_PRINTER);
		mPosPrinter.setOnJustUeventListener(this);
		//mPosPrinter.DeviceOpen(0);
	}*/

	public ImplThermalPrinter(PosDevice inPosDevice){
		mPosPrinter = inPosDevice;
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

	public static String expToString(int errCode) {
		String exceptionMessage;
		switch (errCode) {
			case PRINTER_ERR_SUCCESS:
				exceptionMessage = "Print Success!";
				break;

			case PRINTER_ERR_CONNECT_FAILED:
				exceptionMessage = "Connect To Printer Failed!";
				break;

			case PRINTER_ERR_CMD_TIMEOUT:
				exceptionMessage = "Printer Cmd Timeout";
				break;

			case PRINTER_ERR_NO_PAPER:
				exceptionMessage = "No paper!";
				break;

			case PRINTER_ERR_OVER_TEMPERATURE:
				exceptionMessage = "Over Temperature!";
				break;

			case PRINTER_ERR_OVER_VOLTAGE:
				exceptionMessage = "Over Voltage!";
				break;

			case PRINTER_ERR_USER_CANCEL:
				exceptionMessage = "Canceled";
				break;

			case PRINTER_ERR_INVALID_DATA:
				exceptionMessage = "Invalid Data";
				break;

			case PRINTER_ERR_LOW_POWER:
				exceptionMessage = "Low power error";
				break;
			case PRINTER_ERR_BLOCKING:
				exceptionMessage = "Blocking";
				break;
			case PRINTER_ERR_PRINT_TIMEOUT:
				exceptionMessage = "Print Timeout";
				break;
			default:
				exceptionMessage = "Error:" + errCode;
				break;
		}
		
		return exceptionMessage;
	}

	private static String stateToString(int state){
		String stateString = "";
		switch (state){
		case PRINTER_STATE_IDLE:
			stateString = "PRINTER_STATE_IDLE";
			break;
			
		case PRINTER_STATE_OPEN:
			stateString = "PRINTER_STATE_OPEN";
			break;
			
		case PRINTER_STATE_EXP:
			stateString = "PRINTER_STATE_EXP";
			break;
			
		default:
			stateString = "Undefined State:" + state;
			break;
		}
		return stateString;
	}

	private static String getBatteryInfo(String path) {

		File mFile;
		FileReader mFileReader;
		mFile = new File(path);

		try {
			mFileReader = new FileReader(mFile);
			char data[] = new char[128];
			int charCount;
			String status[] = null;
			try {
				charCount = mFileReader.read(data);
				status = new String(data, 0, charCount).trim().split("\n");
				return status[0];
			} catch (IOException e) {
				Log.i(TAG, "getBatteryInfo IOException !!!!!!");
			}
		} catch (FileNotFoundException e) {
			Log.i(TAG, "getBatteryInfo FileNotFoundException");
		}
		return null;
	}

	private static boolean isBatteryPresent(){
		String VOLTAGE_NOW = "/sys/class/power_supply/battery/voltage_now";

		String batteryInfo = getBatteryInfo(VOLTAGE_NOW);
		Log.i(TAG, "IsBatteryPresent: " + batteryInfo);
		if(batteryInfo.compareTo("3000000")<0){
			return false;
		}else{
			return true;
		}
	}

	private static int getBatteryVoltage(){
		String VOLTAGE_NOW = "/sys/class/power_supply/battery/voltage_now";

		String batteryInfo = getBatteryInfo(VOLTAGE_NOW);
		int valtage = Integer.parseInt(batteryInfo);
		return valtage;
	}

	private void setState(int newState){
		Log.i(TAG, "setState:" + stateToString(newState));
		if(newState < PRINTER_STATE_IDLE || newState > PRINTER_STATE_EXP){
			Log.e(TAG, "setState: Ilvalid newState[" + newState + "]");
		}
		mPrinterState = newState;
	}
	
	/**
	 * get current printer state
	 * @return
	 * 			should be one of:	PRINTER_STATE_IDLE/PRINTER_STATE_OPEN/PRINTER_STATE_EXP
	 */
	public int getState() {
		byte[] thisStateBuff = new byte[32];
		int reval = mPosPrinter.printerGetState(thisStateBuff);
		if (reval != 0){
			return reval;
		}
		mPrinterState = (int)thisStateBuff[8];//UtilFun.bytesToInt(thisStateBuff, 4);
		if(mPrinterState == PRINTER_STATE_EXP){
			if(thisStateBuff[9] == 1){
				mExpValue = PRINTER_ERR_NO_PAPER;
			}
			else if(thisStateBuff[9] == 2){
				mExpValue = PRINTER_ERR_OVER_TEMPERATURE;
			}
		}
		Log.i(TAG, "mPrinterState="+mPrinterState);
		return mPrinterState;
	}
	
	/**
	 * open printer: if there is no print() or goPaper() options within 60 seconds after opening, the printer will be closed automatically
	 * and next time you mast open it again before print or go paper.
	 * @return
	 */
	@Override
	public int open() {
		if (dbg) Log.d(TAG, "open ...");

		if (isBatteryPresent() == false){
			Log.e(TAG, "open: isBatteryPresent() == false" );
			return PRINTER_ERR_LOW_POWER;
		}
		int curVoltage = getBatteryVoltage();
		Log.i(TAG, "curVoltage=" + curVoltage);
		if (curVoltage < 3000000){
			return PRINTER_ERR_LOW_POWER;
		}
		int reval = mPosPrinter.printerOpen(curVoltage);
		if (reval != 0) {
			Log.e(TAG, "open failed: " + expToString(reval));
			mPosPrinter.printerClose();
		}
		setState(PRINTER_STATE_OPEN);
		return 0;
	}

	/**
	 * release printer
	 */
	@Override
	public int close() {
		if (dbg) Log.d(TAG, "close ...");
		int reval = mPosPrinter.printerClose();
		if (reval != 0) {
			Log.e(TAG, "close failed: " + expToString(reval));
			return reval;
		}
		setState(PRINTER_STATE_IDLE);
		return 0;
	}

	private int getPrinterValidBuffLen(){
		byte[] thisByte = new byte[72];
		int reval = mPosPrinter.printerGetState(thisByte);
		if (reval < 0){
			Log.e(TAG, "printerGetState failed: " + expToString(reval));
			return reval;
		}
		int thisLen = UtilFun.bytesToInt32(thisByte, 0) - 4;
		//Log.e(TAG, "printerGetState return len:" + thisLen + " bytes");
		if (thisLen == 2){
			//为兼容SP老版本
			return (100*1024);
		}
		int validBuffLen = UtilFun.bytesToInt32(thisByte, 14);
		Log.i(TAG, "getPrinterValidBuffLen: " +  validBuffLen);
		return validBuffLen;
	}

	/**
	 * print 384 bitmap bytes, you have to open it before print
	 * @param writeByte
	 * @return
	 */
	@Override
	public int print(byte[] writeByte){
		if (dbg) Log.d(TAG, "print(" + writeByte.length + ")...");
		long start = System.currentTimeMillis();
		int dataLen = writeByte.length;
		int cp = 0;

		while(true) {
			int validBuffLen = getPrinterValidBuffLen();
			Log.i(TAG, "SP validBuffLen :" + validBuffLen );
			if (validBuffLen < 0){
				Log.e(TAG, "getState failed: " + expToString(validBuffLen));
				return validBuffLen;
			}else if ((validBuffLen < 2*1024) && (dataLen > validBuffLen)){
				long end = System.currentTimeMillis();
				if ((end - start) >= 30000) {
					Log.e(TAG, "print write timeout(50000ms)");
					return PRINTER_ERR_PRINT_TIMEOUT;
				}

				try {
					Thread.currentThread();
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			int sendLen = dataLen;
			if(dataLen > validBuffLen){
				sendLen = validBuffLen;
			}

			byte[] thisSendBuff = new byte[sendLen];
			byte[] thisOutBuff = new byte[72];
			System.arraycopy(writeByte, cp, thisSendBuff, 0, sendLen);
			Log.i(TAG, "printerWrite: sendLen = " + sendLen );
			int reval = mPosPrinter.printerWrite(thisSendBuff, thisOutBuff);
			if (reval != 0) {
				Log.e(TAG, "printerWrite failed: " + expToString(reval));
				return reval;
			}

			dataLen = dataLen - sendLen;
			Log.e(TAG, "dataLen: " + dataLen );
			if(dataLen == 0){
				mPrintFlag = PRINT_FLAG_MAG_NUMA;
				Log.i(TAG, "write finish");
				return 0;
			}
			cp += sendLen;
			start = System.currentTimeMillis();

		}

	}

	/**
	 * 
	 * @param bitmap
	 * @return
	 */
	public int printLegency(Bitmap bitmap){
		Log.e(TAG, "print()");
		long start = System.currentTimeMillis();
		long end = 0;
		long timeLastLong = 0;
		
		Bitmap thisBitmap = Convertor.imageShrink(bitmap);
		Log.e(TAG, "before Convertor (width=" + bitmap.getWidth() + ",hight=" + bitmap.getHeight() + ")");
		byte [] printBuff = Convertor.convert2PrintBuffer(thisBitmap);
		timeLastLong = System.currentTimeMillis() - start;
		Log.e(TAG, "after Convertor,timeLastLong=" + timeLastLong);
		if(printBuff.length == 0){
			Log.e(TAG, "print(Bitmap) failed: " + expToString(PRINTER_ERR_INVALID_DATA));
			return PRINTER_ERR_INVALID_DATA;
		}
		return print(printBuff);
	}

	/**
	 *
	 * @param bitmap
	 * @return
	 */
	public int print(Bitmap bitmap){
		if (dbg) Log.d(TAG, "print(Bitmap)...");

		Convertor thisConvertor = new Convertor(bitmap);
		thisConvertor.start();

		int dataLen = thisConvertor.getTotalLen();

		Log.e(TAG, "print: dataLen=" + dataLen);
		long start = System.currentTimeMillis();
		int cp = 0;

		int ONE_PACK_LEN = 384*32;
		ONE_PACK_LEN = 48*128;
		int validBuffLen = 0;

		while(!thisConvertor.ready()){
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}


		while(true){
			//if (validBuffLen < ONE_PACK_LEN) {
			validBuffLen = getPrinterValidBuffLen();
			//}
			if(dbg) Log.i(TAG, "SP validBuffLen :" + validBuffLen );
			if (validBuffLen < 0){
				Log.e(TAG, "getState failed: " + expToString(validBuffLen));
				return validBuffLen;
			}else if ((validBuffLen < ONE_PACK_LEN) && (dataLen > validBuffLen)){
				long end = System.currentTimeMillis();
				if ((end - start) >= 30000) {
					Log.e(TAG, "print write timeout(50000ms)");
					return PRINTER_ERR_PRINT_TIMEOUT;
				}

				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			int sendLen = dataLen;

			if(dataLen > validBuffLen){
				sendLen = validBuffLen;
			}

			if(sendLen > ONE_PACK_LEN){
				sendLen = ONE_PACK_LEN;
			}

			byte[] thisSendBuff = thisConvertor.getPrintByte(cp, sendLen);
			if (thisSendBuff == null){
				if (thisConvertor.mWorkingFlag == Convertor.CONVERT_STATUS_END){
					return 0;
				}

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			sendLen = thisSendBuff.length;

			byte[] thisOutBuff = new byte[72];
			if(dbg) Log.i(TAG, "printerWrite: sendLen = " + sendLen );
			if(dataLen == sendLen){
				mPrintFlag = PRINT_FLAG_MAG_NUMA;
			}
			int reval = mPosPrinter.printerWrite(thisSendBuff, thisOutBuff);
			if (reval != 0) {
				Log.e(TAG, "printerWrite failed: " + expToString(reval));
				return reval;
			}
			//validBuffLen = UtilFun.bytesToInt32(thisOutBuff,12);
			dataLen = dataLen - sendLen;
			validBuffLen = validBuffLen - sendLen;
			//if(dbg)Log.e(TAG, "dataLen: " + sendLen );
			if(dataLen == 0){
				Log.i(TAG, "write finish");
				return 0;
			}
			cp += sendLen;
			start = System.currentTimeMillis();
		}

	}

	public void cancel(){
		mPrintFlag = PRINTER_ERR_USER_CANCEL;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isFinished(){
		return (mPrintFlag != PRINT_FLAG_MAG_NUMA);
	}
	
	/**
	 * 
	 * @param timeoutMs
	 * @return
	 */
	public int waitForPrintFinish(int timeoutMs) {
		if (dbg)Log.i(TAG, "waitForPrintFinish: timeoutMs=" + timeoutMs);
		long start = System.currentTimeMillis();
		long end = 0;
		
		while (true) {
			end = System.currentTimeMillis();
			if ((end - start) >= timeoutMs) {
				return PRINTER_ERR_CMD_TIMEOUT;
			}
			
			if (mPrintFlag != PRINT_FLAG_MAG_NUMA){
				if (mExpValue == PRINTER_ERR_SUCCESS){
					Log.e(TAG, "print finished");
					return 0;
				}
				else{
					Log.e(TAG, "print failed:" + expToString(mExpValue));
					return mExpValue;
				}
			}
			try {
	            Thread.sleep(10);
	        } catch (InterruptedException e) {  
	            e.printStackTrace();
	            Log.e(TAG, "waitForPrintFinish: InterruptedException");
	        }
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public int waitForPrintFinish(){
		return waitForPrintFinish(PRINT_DEFAULT_TIMEOUT);
	}


	/**
	 *
	 * @param line
	 * @param unit  the height of line
	 * @return
	 */
	public int freeStep(int line, int unit){
		if (dbg) Log.d(TAG, "freeStep (" +line + "," + unit + ")...");

		if (line <= 0 || unit <= 0){
			Log.e(TAG, "unit cannot < 0 in freeStep()" );
			return PRINTER_ERR_INVALID_DATA;
		}

		mPrintFlag = PRINT_FLAG_MAG_NUMA;
		int reval = mPosPrinter.printerFreeStep(line*unit,1);
		if( reval != 0){
			Log.e(TAG, "print(byte[]) failed: " + expToString(reval));
			mPrintFlag = PRINT_FLAG_MAG_NUMB;
			return reval;
		}

		return 0;
	}

	/**
	 *
	 * @param value
	 * @return
	 */
	public int freeStep(int value){
		return freeStep(value, 32);
	}


	/**
	 *
	 * @param roWidth
	 * @param roBitsPerPixel
	 * @param rwGray      should be 50 -130
	 * @param rwHiTemp   high temperature warning
	 * @param rwLoTemp   low temperature warning
	 * @return
	 */
	public int setPara(int roWidth, int roBitsPerPixel, int rwGray, int rwHiTemp, int rwLoTemp){
		if (dbg) Log.d(TAG, "setPara (" +roWidth + "," + roBitsPerPixel + "," + rwGray + "," + rwHiTemp + "," + "," + rwLoTemp +")...");
		int reval = mPosPrinter.SprinterSetPara(roWidth, roBitsPerPixel, rwGray, rwHiTemp, rwLoTemp);
		if( reval != 0){
			Log.e(TAG, "setPara failed: " + expToString(reval));
			return reval;
		}
		return 0;
	}

	public int setPara(int rwGray, int rwHiTemp, int rwLoTemp){
		return setPara(0, 0, rwGray, rwHiTemp, rwLoTemp);
	}

	public int setPara(int rwGray){
		return setPara(rwGray, 90, -30);
	}

	// ===============================================================================

	@Override
	public void OnJustUevent(int what, int arg, byte[] buff){
		if (what < PosDevice.POS_CMD_PRINTER_BASE || what > PosDevice.POS_CMD_PRINTER_MAX){
			return;
		}
		Log.i(TAG, "OnJustUevent:" + what + ',' + arg);
		switch(what){
		case PosDevice.POS_UNSOLI_PRINTER_STATE_CHANGE:
			int newState = arg;

			if(mPrintFlag == PRINT_FLAG_MAG_NUMA){
				mPrintFlag = PRINT_FLAG_MAG_NUMB;
			}
			if (newState == PRINTER_STATE_EXP){
				byte expByte = buff[1];
				if(expByte == 1){
					setExpValue(PRINTER_ERR_NO_PAPER);
				}
				else if(expByte == 2){
					setExpValue(PRINTER_ERR_OVER_TEMPERATURE);
				}
				else if(expByte == 3){
					setExpValue(PRINTER_ERR_OVER_TEMPERATURE);
				}
				else{
					Log.e(TAG, "OnDeviceStateChange: Undefined expByte:" + expByte);
				}
			}
			else{
				setExpValue(PRINTER_ERR_SUCCESS);
			}
			setState(newState);
			break;

		case PosDevice.POS_UNSOLI_PRINTER_PRINT_FINISH:
			if(mPrintFlag == PRINT_FLAG_MAG_NUMA){
				mPrintFlag = PRINT_FLAG_MAG_NUMB;
				setExpValue(PRINTER_ERR_SUCCESS);
			}
			break;
		default:
			//Log.e(TAG, "OnJustUevent:Undefined event[" + what + "]");
			break;
		}
	}

}