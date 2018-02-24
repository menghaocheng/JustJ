package com.justtide.aidl;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public final class ResponseApdu implements Parcelable{
    /** @serial */
    private byte[] apdu;

    public static final Creator<ResponseApdu> CREATOR = new Creator<ResponseApdu>() {

        @Override
        public ResponseApdu[] newArray(int size) {
            return new ResponseApdu[size];
        }

        @Override
        public ResponseApdu createFromParcel(Parcel source) {
            return new ResponseApdu(source);
        }
    };

    /**
     * Constructs a ResponseAPDU from a byte array containing the complete
     * APDU contents (conditional body and trailed).
     *
     * <p>Note that the byte array is cloned to protect against subsequent
     * modification.
     *
     * @param apdu the complete response APDU
     *
     * @throws NullPointerException if apdu is null
     * @throws IllegalArgumentException if apdu.length is less than 2
     */
    public ResponseApdu(byte[] apdu) {
		apdu = apdu.clone();
		check(apdu);
		this.apdu = apdu;
    }

    public ResponseApdu(Parcel source) {

        int thisApduLen = source.readInt();

        if (thisApduLen != 0){
            apdu = new byte[thisApduLen];
            source.readByteArray(apdu);
        }

    }
    
    private static void check(byte[] apdu) {
		if (apdu.length < 2) {
		    throw new IllegalArgumentException("apdu must be at least 2 bytes long");
		}
    }

    /**
     * Returns the number of data bytes in the response body (Nr) or 0 if this 
     * APDU has no body. This call is equivalent to
     * <code>getData().length</code>.
     *
     * @return the number of data bytes in the response body or 0 if this APDU
     * has no body.
     */
    public int getNr() {
    	return apdu.length - 2;
    }

    /**
     * Returns a copy of the data bytes in the response body. If this APDU as
     * no body, this method returns a byte array with a length of zero.
     *
     * @return a copy of the data bytes in the response body or the empty
     *    byte array if this APDU has no body.
     */
    public byte[] getData() {
		byte[] data = new byte[apdu.length - 2];
		System.arraycopy(apdu, 0, data, 0, data.length);
		return data;
    }

    /**
     * Returns the value of the status byte SW1 as a value between 0 and 255.
     *
     * @return the value of the status byte SW1 as a value between 0 and 255.
     */
    public int getSW1() {
    	return apdu[apdu.length - 2] & 0xff;
    }

    /**
     * Returns the value of the status byte SW2 as a value between 0 and 255.
     *
     * @return the value of the status byte SW2 as a value between 0 and 255.
     */
    public int getSW2() {
    	return apdu[apdu.length - 1] & 0xff;
    }

    /**
     * Returns the value of the status bytes SW1 and SW2 as a single
     * status word SW.
     * It is defined as
     * <code>(getSW1() << 8) | getSW2()</code>.
     *
     * @return the value of the status word SW.
     */
    public int getSW() {
    	return (getSW1() << 8) | getSW2();
    }

    /**
     * Returns a copy of the bytes in this APDU.
     *
     * @return a copy of the bytes in this APDU.
     */
    public byte[] getBytes() {
        return apdu.clone();
    }

    public byte[] getBytesWithLength(){
        byte[] thisByte = new byte[apdu.length + 3];
        int cp = 0;
        UtilFun.int16ToBytes(thisByte, cp, apdu.length);
        cp += 2;
        System.arraycopy(apdu, 0, thisByte, cp, apdu.length);
        cp += apdu.length;
        thisByte[cp] = 0;
        return thisByte;
    }

    public byte[] getBytesEndWith00(){
        byte[] thisByte = new byte[apdu.length + 1];
        int cp = 0;
        //UtilFun.int16ToBytes(thisByte, cp, apdu.length);
        //cp += 2;
        System.arraycopy(apdu, 0, thisByte, cp, apdu.length);
        cp += apdu.length;
        thisByte[cp] = 0;
        return thisByte;
    }

    /**
     * Returns a string representation of this response APDU.
     *
     * @return a String representation of this response APDU.
     */
    public String toString() {
		return "ResponseAPDU: " + apdu.length + " bytes, SW="
		    + Integer.toHexString(getSW());
    }
    
    /**
     * Compares the specified object with this response APDU for equality. 
     * Returns true if the given object is also a ResponseAPDU and its bytes are
     * identical to the bytes in this ResponseAPDU.
     *
     * @param obj the object to be compared for equality with this response APDU
     * @return true if the specified object is equal to this response APDU
     */
    public boolean equals(Object obj) {
		if (this == obj) {
		    return true;
		}
		if (obj instanceof ResponseApdu == false) {
		    return false;
		}
		ResponseApdu other = (ResponseApdu)obj;
		return Arrays.equals(this.apdu, other.apdu);
		
    }
    
    /**
     * Returns the hash code value for this response APDU.
     *
     * @return the hash code value for this response APDU.
     */
    public int hashCode() {
    	return Arrays.hashCode(apdu);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int thisApduLen = 0;

        if (apdu != null){
            thisApduLen = apdu.length;
        }
        dest.writeInt(thisApduLen);
        if(apdu != null){
            dest.writeByteArray(apdu);
        }
    }
}
