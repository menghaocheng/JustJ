package com.justtide.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mc.meng on 2017/8/15.
 */

public class HsmObj implements Parcelable {

    public final int HSM_BYTE_MIN_LEN  = 104;
    public final int HSM_DATA_MAXLEN  = (4*1024);

    /*
    public enum HSM_OBJECT_TYPE {
        HSM_OBJECT_TYPE_invalid  ,	//无效值
        HSM_OBJECT_TYPE_private_key,    //私钥证书
        HSM_OBJECT_TYPE_public_key,    //公钥证书
        HSM_OBJECT_TYPE_cert,          //CERT证书
    }

    public enum HSM_OBJECT_DATA_TYPE {
        HSM_OBJECT_DATA_TYPE_invalid , 	//无效值
        HSM_OBJECT_DATA_TYPE_pem, 		//pem证书格式
        HSM_OBJECT_DATA_TYPE_der, 		//der编码证书格式
        HSM_OBJECT_DATA_TYPE_p7d, 		//PKCS #7证书格式
        HSM_OBJECT_DATA_TYPE_pfx  		//PKCS #12证书格式
    }
    */

    //证书类型
    public static final int HSM_OBJECT_TYPE_private_key   = 0; //私钥证书
    public static final int HSM_OBJECT_TYPE_public_key    = 1;//公钥证书
    public static final int HSM_OBJECT_TYPE_cert          = 2;//CERT证书

    public static final int HSM_OBJECT_DATA_TYPE_pem      = 0;  //pem证书格式
    public static final int HSM_OBJECT_DATA_TYPE_der	  = 1;  //der编码证书格式
    public static final int HSM_OBJECT_DATA_TYPE_p7d      = 2;  //PKCS #7证书格式
    public static final int HSM_OBJECT_DATA_TYPE_pfx      = 3;  //PKCS #12证书格式

    public byte[] mIndex = new byte[32];
    public byte[] mName = new byte[32];
    public byte[] mPassword = new byte[32];
    public int mObjectType;

    public static final Creator<HsmObj> CREATOR = new Creator<HsmObj>() {

        @Override
        public HsmObj[] newArray(int size) {
            return new HsmObj[size];
        }

        @Override
        public HsmObj createFromParcel(Parcel source) {
            return new HsmObj(source);
        }
    };

    public HsmObj(int objectType) {
        //mIndex = new byte[32];
        //mName = new byte[32];
        //mPassword = new byte[32];
        mObjectType = objectType;
    }

    public HsmObj(Parcel source) {
        source.readByteArray(mIndex);
        source.readByteArray(mName);
        source.readByteArray(mPassword);
        mObjectType = source.readInt();
    }

    public int setIndex(byte[] index){
        if (index.length > mIndex.length){
            return -1;
        }
        System.arraycopy(index, 0, mIndex, 0, index.length);
        return 0;
    }

    public int setName(byte[] name){
        if (name.length > mName.length){
            return -1;
        }
        System.arraycopy(name, 0, mName, 0, name.length);
        return 0;
    }

    public int setPassword(byte[] password){
        if (password.length > mPassword.length){
            return -1;
        }
        System.arraycopy(password, 0, mPassword, 0, password.length);
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(mIndex);
        dest.writeByteArray(mName);
        dest.writeByteArray(mPassword);
        dest.writeInt(mObjectType);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
