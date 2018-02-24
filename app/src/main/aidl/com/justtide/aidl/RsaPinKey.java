package com.justtide.aidl;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by mc.meng on 2017/10/10.
 */

public class RsaPinKey implements Parcelable{
    private static final String TAG = "RsaPinKey";
    public static final int MOD_MAXLEN = 256;
    public static final int EXP_MAXLEN = 4;
    public static final int RANDOM_MAXLEN = 8;


    public int modlen;
    public byte[] mod;
    public int explen;
    public byte[] exp;
    public byte[] iccrandom;


    public static final Creator<RsaPinKey> CREATOR = new Creator<RsaPinKey>() {

        @Override
        public RsaPinKey[] newArray(int size) {
            return new RsaPinKey[size];
        }

        @Override
        public RsaPinKey createFromParcel(Parcel source) {
            return new RsaPinKey(source);
        }
    };

    public RsaPinKey(){
        modlen = 0;
        mod = new byte[MOD_MAXLEN];
        explen = 0;
        exp = new byte[4];
        iccrandom = new byte[8];

    }

    public RsaPinKey(Parcel source) {
        modlen = source.readInt();
        if(modlen != 0) {
            source.readByteArray(mod);
        }
        else{
            mod = null;
        }
        explen = source.readInt();
        if(explen != 0){
            source.readByteArray(exp);
        }
        else{
            exp = null;
        }

        int thisIccrandomLen = source.readInt();
        if(thisIccrandomLen != 0){
            iccrandom = new byte[thisIccrandomLen];
            source.readByteArray(iccrandom);
        }
        else{
            iccrandom = null;
        }

    }

    public byte[] getBytes(){
        byte[] thisByte = new byte[4 + mod.length + 4 + exp.length + iccrandom.length];
        int cp = 0;
        UtilFun.int32ToBytes(thisByte, cp, modlen);
        cp += 4;
        System.arraycopy(mod, 0, thisByte, cp, mod.length);
        cp += mod.length;
        UtilFun.int32ToBytes(thisByte, cp, explen);
        cp += 4;
        System.arraycopy(exp, 0, thisByte, cp, exp.length);
        cp += exp.length;
        System.arraycopy(iccrandom, 0, thisByte, cp, iccrandom.length);
        cp += iccrandom.length;
        return thisByte;
    }

    public boolean setMod(byte[] inModeData){
        if (inModeData == null || inModeData.length > MOD_MAXLEN){
            Log.e(TAG, "setMod: invalid para in setMod" );
            return false;
        }
        mod = new byte[MOD_MAXLEN];
        System.arraycopy(inModeData, 0, mod, 0, inModeData.length);
        modlen = inModeData.length;
        return true;
    }

    public boolean setExp(byte[] inExp){
        if (inExp == null || inExp.length > EXP_MAXLEN){
            Log.e(TAG, "setMod: invalid para in setExp" );
            return false;
        }
        exp = new byte[EXP_MAXLEN];
        System.arraycopy(inExp, 0, exp, 0, inExp.length);
        explen = inExp.length;
        return true;
    }

    public boolean setRandom(byte[] inRandom){
        if (inRandom == null || inRandom.length > RANDOM_MAXLEN){
            Log.e(TAG, "setMod: invalid para in setRandom" );
            return false;
        }
        iccrandom = new byte[RANDOM_MAXLEN];
        System.arraycopy(inRandom, 0, iccrandom, 0, inRandom.length);
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(modlen);
        if(modlen != 0){
            dest.writeByteArray(mod);
        }

        dest.writeInt(explen);
        if(explen != 0){
            dest.writeByteArray(exp);
        }
        if(iccrandom != null){
            dest.writeInt(iccrandom.length);
            dest.writeByteArray(iccrandom);
        }

    }
}
