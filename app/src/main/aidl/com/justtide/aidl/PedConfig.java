package com.justtide.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class PedConfig implements Parcelable{
	public static final int MODEL_BUFFLEN = 16;
	public static final int HWVER_BUFFLEN = 16;
	public static final int SWVER_BUFFLEN = 16;
	public static final int RES_BUFFLEN = 154;
	
	byte[]		  mModel;        // 机器型号
	byte[]		  mHwVer;        // 硬件版本信息
	byte[]		  mSwVer;        // 软件版本信息
	
	public int	  mStatus;       // PED状态
	public int	  mMaxBps;       // PED最高支持的通讯波特率
	public int	  mMaxAppNum;    // 最大可支持的应用数
	public int	  mMasterKeyNum; // 最大MK存储数
	public int	  mPINKeyNum;    // 最大PIN Key存储数
	
	public int	  mMACKeyNum;    // 最大MAC Key存储数
	public int	  mFixPINKeyNum; // 最大Fix PIN Key存储数
	public int	  mFixMACKeyNum; // 最大Fix MAC Key存储数
	public int	  mDukptKeyNum;  // 最大Dukpt Key存储数
	public int	  mCtime;        // 密钥生成时间
	
	public int	  mMagKeyTime;	// 磁卡密钥生成时间
	public int	  mMagKeyStatus;// 磁卡密钥是否有效
	
	public int  mMaxTemp;    // 高温限制
	public int  mMinTemp;    // 低温限制
	public int  mCurTemp;    // 当前CPU的温度
	byte[]	    mRes;        // 预留

	public static final Creator<PedConfig> CREATOR = new Creator<PedConfig>() {

		@Override
		public PedConfig[] newArray(int size) {
			return new PedConfig[size];
		}

		@Override
		public PedConfig createFromParcel(Parcel source) {
			return new PedConfig(source);
		}
	};

	public PedConfig(byte[] inBytes, int offset, int cfgLen){
		int cp = offset;
		mModel = new byte[MODEL_BUFFLEN];
		mHwVer = new byte[HWVER_BUFFLEN];
		mSwVer = new byte[SWVER_BUFFLEN];
		System.arraycopy(inBytes, cp, mModel, 0, MODEL_BUFFLEN);
		cp += MODEL_BUFFLEN;
		System.arraycopy(inBytes, cp, mHwVer, 0, HWVER_BUFFLEN);
		cp += HWVER_BUFFLEN;
		System.arraycopy(inBytes, cp, mSwVer, 0, SWVER_BUFFLEN);
		cp += SWVER_BUFFLEN;
		
		mStatus = UtilFun.bytesToInt32(inBytes, cp);
		cp += 4;
		mMaxBps = UtilFun.bytesToInt32(inBytes, cp);
		cp += 4;
		mMaxAppNum = UtilFun.bytesToInt32(inBytes, cp);
		cp += 4;
		mMasterKeyNum = UtilFun.bytesToInt32(inBytes, cp);
		cp += 4;
		mPINKeyNum = UtilFun.bytesToInt32(inBytes, cp);
		cp += 4;
		
		mMACKeyNum = UtilFun.bytesToInt32(inBytes, cp);
		cp += 4;
		mFixPINKeyNum = UtilFun.bytesToInt32(inBytes, cp);
		cp += 4;
		mFixMACKeyNum = UtilFun.bytesToInt32(inBytes, cp);
		cp += 4;
		mDukptKeyNum = UtilFun.bytesToInt32(inBytes, cp);
		cp += 4;
		mCtime = UtilFun.bytesToInt32(inBytes, cp);
		cp += 4;
		
		mMagKeyTime = UtilFun.bytesToInt32(inBytes, cp);
		cp += 4;
		mMagKeyStatus = UtilFun.bytesToInt32(inBytes, cp);
		cp += 4;
		
		mMaxTemp = UtilFun.bytesToInt32(inBytes, cp);
		cp += 2;
		mMinTemp = UtilFun.bytesToInt32(inBytes, cp);
		cp += 2;
		mCurTemp = UtilFun.bytesToInt32(inBytes, cp);
		cp += 2;
		
		int resLen = cfgLen - cp;
		mRes = new byte[resLen];
		System.arraycopy(inBytes, cp, mRes, 0, resLen);
	}

	public PedConfig(Parcel source) {
		int thismModelLen = source.readInt();
		mModel = new byte[thismModelLen];
		source.readByteArray(mModel);
		int thismHwVerLen = source.readInt();
		mHwVer = new byte[thismHwVerLen];
		source.readByteArray(mHwVer);
		int thisMSwVerLen = source.readInt();
		mSwVer = new byte[thisMSwVerLen];
		source.readByteArray(mSwVer);

		mStatus = source.readInt();
		mMaxBps = source.readInt();
		mMaxAppNum = source.readInt();
		mMasterKeyNum = source.readInt();
		mPINKeyNum = source.readInt();
		mMACKeyNum = source.readInt();
		mFixPINKeyNum = source.readInt();
		mFixMACKeyNum = source.readInt();
		mDukptKeyNum = source.readInt();
		mCtime = source.readInt();
		mMagKeyTime = source.readInt();
		mMagKeyStatus = source.readInt();
		mMaxTemp = source.readInt();
		mMinTemp = source.readInt();
		mCurTemp = source.readInt();
		mCurTemp = source.readInt();
		int thismResLen = source.readInt();
		mRes = new byte[thismResLen];
		source.readByteArray(mRes);

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		int thisLen = 0;
		if(mModel != null){
			thisLen = mModel.length;
		}
		dest.writeInt(thisLen);
		dest.writeByteArray(mModel);

		thisLen = 0;
		if(mHwVer != null){
			thisLen = mHwVer.length;
		}
		dest.writeInt(thisLen);
		dest.writeByteArray(mHwVer);

		thisLen = 0;
		if(mSwVer != null){
			thisLen = mSwVer.length;
		}
		dest.writeInt(thisLen);
		dest.writeByteArray(mSwVer);

		dest.writeInt(mStatus);
		dest.writeInt(mMaxBps);
		dest.writeInt(mMaxAppNum);
		dest.writeInt(mMasterKeyNum);
		dest.writeInt(mPINKeyNum);

		dest.writeInt(mMACKeyNum);
		dest.writeInt(mFixPINKeyNum);
		dest.writeInt(mFixPINKeyNum);
		dest.writeInt(mDukptKeyNum);
		dest.writeInt(mCtime);

		dest.writeInt(mMagKeyTime);
		dest.writeInt(mMagKeyStatus);

		dest.writeInt(mMaxTemp);
		dest.writeInt(mMinTemp);
		dest.writeInt(mCurTemp);

		thisLen = 0;
		if(mRes != null){
			thisLen = mRes.length;
		}
		dest.writeInt(thisLen);
		dest.writeByteArray(mRes);

	}
}