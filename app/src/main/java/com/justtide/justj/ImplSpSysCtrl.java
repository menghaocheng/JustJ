package com.justtide.justj;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.just.api.PosDevice;
import com.just.api.UtilFun;
import com.justtide.aidl.ISpSysCtrl;
import com.justtide.aidl.Time;

/**
 * Created by mc.meng on 2018/2/24.
 */

public class ImplSpSysCtrl extends ISpSysCtrl.Stub {
    private static final String TAG = "ImplSpSysCtrl";
    private static boolean dbg = true;

    public static final int SPSYS_ERR_BASE = PosDevice.SPSYS_ERR_BASE;
    public static final int SPSYS_ERR_SUCCESS            = 0;
    public static final int SPSYS_ERR_CONNECT_FAILED     = SPSYS_ERR_BASE - 1;
    public static final int SPSYS_ERR_CMD_TIMEOUT        = SPSYS_ERR_BASE - 2;
    public static final int SPSYS_ERR_INVALID_SN         = SPSYS_ERR_BASE - 3;


    public static final int SPSYS_SN_MAXLEN     = 20;

    public static final int LED_NAME_BLUE     = 0x01;
    public static final int LED_NAME_YELLOW   = 0x02;
    public static final int LED_NAME_GREEN    = 0x04;
    public static final int LED_NAME_RED      = 0x08;
    public static final int LED_NAME_ALL      = 0x0F;

    public static final int LED_MODE_OFF      = 0x00000000;
    public static final int LED_MODE_ON       = 0x10000000;
    public static final int LED_MODE_FLASH    = 0x20000000;

    PosDevice mPosDevice = null;

    private int mExpValue = SPSYS_ERR_SUCCESS;

    public ImplSpSysCtrl(PosDevice inPosDevice){
        mPosDevice = inPosDevice;
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
            case SPSYS_ERR_SUCCESS:
                exceptionMessage = "Success!";
                break;

            case SPSYS_ERR_CONNECT_FAILED:
                exceptionMessage = "Connect To Sp Failed!";
                break;

            case SPSYS_ERR_CMD_TIMEOUT:
                exceptionMessage = "Sys Cmd Timeout";
                break;

            case SPSYS_ERR_INVALID_SN:
                exceptionMessage = "Invalid sn length";
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
        int reval = mPosDevice.sysHandShake(thisByte);
        if (reval < 0){
            Log.e(TAG, "getVersion Failed. reval = "+ reval);
            setExpValue(reval);
            return null;
        }

        int versionLen = (thisByte[0] & 0x000000FF) - 4;
        String versionStr = UtilFun.byteToString(thisByte, 8, versionLen);
        return versionStr;

    }

    /**
     * reboot sp
     * @return
     */
    @Override
    public int reboot(){
        if (dbg) Log.d(TAG, "reboot ...");
        int reval = mPosDevice.sysReboot();
        if (reval < 0){
            Log.e(TAG, "sysReboot failed:" + expToString(reval));
            return reval;
        }
        return 0;
    }

    @Override
    public int beep(int frequncy, int timeMs){
        if (dbg) Log.d(TAG, "beep(" + frequncy + "," + timeMs + ") ...");
        int reval = mPosDevice.sysBeep(frequncy, timeMs);
        if (reval < 0){
            Log.e(TAG, "beep failed:" + expToString(reval));
            return reval;
        }
        return 0;
    }

    /**
     *  Set android system's time and sync it to SP
     * @param time  input time param, if this param is null, it will get the Linux's system time to sp only
     * @return
     */
    @Override
    public int setTime(Time time) {
        if (dbg) Log.d(TAG, "setTime(time) ...");
        byte[] thisByte = new byte[0];
        if(time != null){
            thisByte = time.getBytes();
        }
        int reval = mPosDevice.sysSetTime(thisByte);
        if (reval < 0){
            Log.e(TAG, "sysSetTime failed" + expToString(reval));
            return reval;
        }
        return 0;
    }

    /**
     * get the sp's system time
     * @return
     */
    @Override
    public Time getTime(){
        if (dbg) Log.d(TAG, "getTime ...");
        byte[] thisByte = new byte[256];
        int reval = mPosDevice.sysGetTime(thisByte);
        if (reval < 0){
            Log.e(TAG, "sysGetTime failed:" + expToString(reval));
            setExpValue(reval);
            return null;
        }
        return new Time(thisByte, 8);
    }


    /**
     *
     * @param ledName
     * 			should be one of:LED_NAME_BLUE/LED_NAME_YELLOW/LED_NAME_GREEN/LED_NAME_RED/LED_NAME_ALL
     * @param ledMode
     * 			should be one of:LED_MODE_OFF/LED_MODE_ON/LED_MODE_FLASH
     * @return
     */
    @Override
    public int ledControl(int ledName, int ledMode){
        if (dbg) Log.d(TAG, "ledControl("+ledName+","+ledMode +") ...");
        int reval = mPosDevice.sysLedCtrl(ledName, ledMode);
        if (reval < 0){
            Log.e(TAG, "sysLedCtrl failed:" + expToString(reval));
            return reval;
        }

        return 0;
    }

    /**
     * snLen is 20 in max
     * @return
     */
    @Override
    public byte[] getSn(){
        if (dbg) Log.d(TAG, "getSn ...");
        byte[] thisByte = new byte[64];
        int reval = mPosDevice.sysGetSn(thisByte);
        if (reval < 0){
            Log.e(TAG, "sysLedCtrl sysGetSn:" + expToString(reval));
            setExpValue(reval);
            return null;
        }
        int snLen = UtilFun.bytesToInt32(thisByte, 0) - 4;
        byte[] thisSn = new byte[snLen];
        System.arraycopy(thisByte, 8, thisSn, 0, snLen);
        return thisSn;
    }

    /**
     *
     * @param sn
     * 			should be less than SPSYS_SN_MAXLEN
     * @return
     *
     */

    @Override
    public int setSn(byte[] sn){
        if (dbg) Log.d(TAG, "setSn ...");
        if (sn.length > SPSYS_SN_MAXLEN){
            Log.e(TAG, "invalid sn len:"+sn.length);
            return SPSYS_ERR_INVALID_SN;
        }
        int reval = mPosDevice.sysSetSn(sn);
        if (reval < 0){
            Log.e(TAG, "sysSetSn failed:" + expToString(reval));
            return reval;
        }
        return 0;
    }

    @Override
    public int spLogOpen(){
        if (dbg) Log.d(TAG, "spLogOpen ...");

        int reval = mPosDevice.sysSpLogOpen();
        if (reval < 0){
            Log.e(TAG, "spLogOpen failed:" + expToString(reval));
            return reval;
        }
        return 0;
    }

    @Override
    public int spLogClose(){
        if (dbg) Log.d(TAG, "spLogClose ...");

        int reval = mPosDevice.sysSpLogClose();
        if (reval < 0){
            Log.e(TAG, "spLogClose failed:" + expToString(reval));
            return reval;
        }
        return 0;
    }

    @Override
    public int exeRootCmd(String cmdStr){
        if (dbg) Log.d(TAG, "exeRootCmd ...");

        int reval = mPosDevice.monitorExeRootCmd(cmdStr.getBytes());
        if (reval < 0){
            Log.e(TAG, "monitorExeRootCmd failed:" + expToString(reval));
            return reval;
        }
        return 0;
    }

    @Override
    public int exeCmd(String cmdStr){
        if (dbg) Log.d(TAG, "exeCmd ...");

        int reval = mPosDevice.monitorExeCmd(cmdStr.getBytes());
        if (reval < 0){
            Log.e(TAG, "monitorExeCmd failed:" + expToString(reval));
            return reval;
        }
        return 0;
    }

}
