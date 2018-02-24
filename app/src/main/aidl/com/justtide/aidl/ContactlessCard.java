package com.justtide.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public final class ContactlessCard implements Parcelable{


	/**
	 * An ISO 14443-3A card
	 */
	public static final byte NFCCARD_TYPE_A = 0x00;
	/**
	 * An ISO 14443-3B card
	 */
	public static final byte NFCCARD_TYPE_B = 0x01;
	/**
	 * FeliCa, 212kbits/s
	 */
	public static final byte NFCCARD_TYPE_C1 = 0x02;
	
	/**
	 * FeliCa, 424kbits/s
	 */
	public static final byte NFCCARD_TYPE_C2 = 0x03;


	public static final byte NFCCARD_TYPE_M50 = 0x04;


	public static final byte NFCCARD_TYPE_M70 = 0x05;

	public static final byte NFCCARD_TYPE_FELICA  = 0x0b;  //Sony FeliCa

	public static final byte NFCCARD_TYPE_CN_ID = 0x0c;  //chinese ID card


	/**
	 * The type of this card is unknown
	 */
	public static final byte NFCCARD_TYPE_UNKOWN = (byte)0xFF;

	/**
	 * The card is deactivated
	 */
	//public static final int IS_DEACTIVATED = 0;
	public static final int IS_IDLE = 1;
	public static final int IS_READY = 2;
	/**
	 * The card has been activated
	 */
	public static final int IS_ACTIVATED = 3;
	/**
	 * The card has been halted
	 */
	public static final int IS_HALTED = 4;
	
	public static final int NFC_STATE_CLOSED = 0;
	public static final int NFC_STATE_OPEN = 1;
	public static final int NFC_STATE_POLLING = 2;
	public static final int NFC_STATE_READY = 3;
	public static final int NFC_STATE_REMOVING = 4;

	private byte cardType;
	private byte[] serialNo;
	private int state;


	public static final Creator<ContactlessCard> CREATOR = new Creator<ContactlessCard>() {

		@Override
		public ContactlessCard[] newArray(int size) {
			return new ContactlessCard[size];
		}

		@Override
		public ContactlessCard createFromParcel(Parcel source) {
			return new ContactlessCard(source);
		}
	};

	public ContactlessCard(int state, byte cardType, byte[] serialNo){
		this.cardType = cardType;
		this.serialNo = serialNo.clone();
		this.state = state;
	}
	
	public ContactlessCard(byte[] buff, int offset){
		this.cardType = buff[offset];
		byte serialNoLength = buff[offset+1];
		this.serialNo = new byte[serialNoLength];
		System.arraycopy(buff, offset+2, this.serialNo, 0, serialNoLength);
		this.state = IS_ACTIVATED;
	}
	
	public ContactlessCard(){
		cardType = NFCCARD_TYPE_UNKOWN;
		serialNo = new byte[0];
		state = NFC_STATE_CLOSED;
	}

	public ContactlessCard(Parcel source) {

		cardType  = source.readByte();
		int thisSerialNoLen = source.readInt();
		if(thisSerialNoLen > 0){
			serialNo = new byte[thisSerialNoLen];
			source.readByteArray(serialNo);
		}
		else{
			serialNo = null;
		}
		state = source.readInt();

	}
	
	/**
     * Get the type of this card.
     *
     * @return TYPE_A it's an ISO 14443-3A card
     *        <p>TYPE_B it's an ISO 14443-3B card
     *        <p>TYPE_M it's a mifare card
     *        <p>TYPE_UNKOWN the type of this card is unknown
     */
	public byte getType(){
		return cardType;
	}
	
	/**
     * Get the serial number of this card.
     *
     * @return serial number of this card
     */
	public byte[] getSerialNo(){
		return serialNo;
	}
	
	/**
     * Get the state of this card.
     *
     * @return IS_DEACTIVATED this card is deactivated
     *         <p>IS_ACTIVATED this card has been activated
     *         <p>IS_HALT this card has been halted
     */
	public int getState(){
		return state;
	}
	
	 void setState(int state){
		this.state = state;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte(cardType);
		int thisSerialNo = 0;
		if(serialNo != null){
			thisSerialNo = serialNo.length;
		}
		dest.writeInt(thisSerialNo);
		dest.writeByteArray(serialNo);
		dest.writeInt(state);

	}
}
