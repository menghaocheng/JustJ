package com.justtide.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a contact smart card that has been discovered.
 */
public final class ContactCard implements Parcelable{
	
	/**
	 * The card is out of slot. 
	 */
	public static final int IS_ABSENT = 0;
	
	/**
	 * The card is in the slot.
	 */
	public static final int IS_PRESENT = 1;
	
	/**
	 * The card has been reset.
	 */
	public static final int IS_ENABLED = 2;
	
	/**
	 * The card is not reset.
	 */
	public static final int IS_DISABLED = 3;
	
	private byte slot;
	private byte[] atr;
	private byte vccMode;
	private boolean emvMode;
	private int state;
	private int startHistorical, nHistorical;
	
	private Convention convention;
	private Protocol protocol;


	public static final Creator<ContactCard> CREATOR = new Creator<ContactCard>() {

		@Override
		public ContactCard[] newArray(int size) {
			return new ContactCard[size];
		}

		@Override
		public ContactCard createFromParcel(Parcel source) {
			return new ContactCard(source);
		}
	};


	/**
     * Constructor to be used by internal classes.
     * @hide
     */
	public ContactCard(byte slot, byte vccMode, boolean emvMode, byte[] atr) {
		this.slot = slot;
		this.atr = atr.clone();
		this.vccMode = vccMode;
		this.emvMode = emvMode;
		state = IS_ENABLED;
		parse();
	}

	public ContactCard(Parcel source) {
		slot = source.readByte();
		int thisAtrLen = source.readInt();
		if(thisAtrLen != 0){
			atr = new byte[thisAtrLen];
			source.readByteArray(atr);
		}

		vccMode = source.readByte();
		boolean[] thisEmvMode = new boolean[1];
		source.readBooleanArray(thisEmvMode);
		emvMode = thisEmvMode[0];
		state = source.readInt();
		startHistorical = source.readInt();
		nHistorical = source.readInt();
		convention = Convention.valueOf(source.readString());
		protocol = Protocol.valueOf(source.readString());
	}

	/**
	 * Indicates the coding conventions of the card.
	 */
	public static enum Convention{
		/**
		 * Indicates inverse convention.
		 */
        INVERSE,
        
        /**
         * Indicates direct convention.
         */
        DIRECT
    };
	
	/**
	 * Indicates the protocol of the card.
	 */
	public static enum Protocol{
		/**
		 * Indicates T=0 protocol.
		 */
		T_0,
		
		/**
		 * Indicates T=1 protocol.
		 */
		T_1;
		
		public String toString(){
            switch(this){
                case T_0:
                    return "T=0";
                case T_1:
                    return "T=1";
            }
            return "";
        }
	};
	
    private void parse() {

            convention = Convention.DIRECT;

		    startHistorical = 0;
		    nHistorical = 0;

			protocol = Protocol.T_0;
		
    }
    
    private void parse_ok() {
		if (atr.length < 2) {
			throw new IllegalArgumentException("ATR is to short");
		}
		if ((atr[0] != 0x3b) && (atr[0] != 0x3f)) {
		    return;
		}
		if(atr[0] == (byte)0x3B){
            convention = Convention.DIRECT;
        }else if(atr[0] == (byte)0x3F){
            convention = Convention.INVERSE;
        }else{
        	throw new IllegalArgumentException("Unable to parse ATR according ISO 7816");
        }
		
		int t0 = (atr[1] & 0xf0) >> 4;
		int n = atr[1] & 0xf;
		int i = 2;
		int cardProtocol = 0; // default protocol is T=0
		while ((t0 != 0) && (i < atr.length)) {
		    if ((t0 & 1) != 0) {
		    	i++;
		    }
		    if ((t0 & 2) != 0) {
		    	i++;
		    }
		    if ((t0 & 4) != 0) {
		    	i++;
		    }
		    if ((t0 & 8) != 0) {
				if (i >= atr.length) {
					throw new IllegalArgumentException
					("Unable to parse ATR according ISO 7816 (i >= atr.length)");
				}
				byte TDi = atr[i++]; //TD1 or TD2
				t0 = (TDi & 0xf0) >> 4;
				cardProtocol |= TDi & 0x01;  // if one bit1 of TDi is 1, it means T=1 protocol
		    } else {
		    	t0 = 0;
		    }
		}
		int k = i + n;
		if ((k == atr.length) || (k == atr.length - 1)) {
		    startHistorical = i;
		    nHistorical = n;
		}
		if(cardProtocol == 0){
			protocol = Protocol.T_0;
		}else{
			protocol = Protocol.T_1;
		}
    }
    
    /**
     * Returns the ATR of this card.
     *
     * @return the ATR of this card.
     */
    public byte[] getATR(){
    	return atr;
    }
    
    /**
     * Returns the coding conventions of this card.
     *
     * @return the coding conventions of this card.
     */
    public Convention getConvention(){
        return convention;
    }
    
    /**
     * Returns the protocol in use for this card.
     *
     * @return the protocol in use for this card.
     */
    public Protocol getProtocol(){
    	return protocol;
    }

    /**
     * Returns a copy of the historical bytes in this ATR.
     * If this ATR does not contain historical bytes, an array of length
     * zero will be returned.
     *
     * @return a copy of the historical bytes in this ATR.
     */
    public byte[] getHistoricalBytes() {
		byte[] b = new byte[nHistorical];
		System.arraycopy(atr, startHistorical, b, 0, nHistorical);
		return b;
    }
    
    /**
     * Returns the Card slot for the basic logical channel. 
     * <p>The user smart card slot is always 0. And PSAM card slot will be 1 to 4.
     * 
     * @return the slot of this card
     */
    public byte getSlot(){
    	return slot;
    }

    /**
     * Returns the card power supply voltage. 
     * 
     * @return card power supply voltage
     */
    public byte getVccMode(){
    	return vccMode;
    }
    
    /**
     * Returns true if the card complies EMV standard. 
     * <p> If this method returns false, the card is only in 
     * compliance with ISO standard.
     *  
     * @return true if the Card is an EMV card
     */
    public boolean isEMVCard(){
    	return emvMode;
    }
    
    /**
     * Returns the state of the Card.
     * @return IS_ABSENT the card is out of slot
     *         <p>IS_PRESENT the card is in the slot
     *         <p>IS_ENABLE the card has been reset
     *         <p>IS_DISABLE the card is not reset 
     */
    public int getState(){
    	return state;
    }
    
    /**
     * Set the state of the Card.
     * 
     * @param state card state
     */
    void setState(int state){
    	this.state = state;
    }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte(slot);
		int thisAtrLen = 0;
		if(atr != null){
			thisAtrLen = atr.length;
		}
		dest.writeInt(thisAtrLen);
		dest.writeByteArray(atr);
		dest.writeByte(vccMode);
		boolean thisEmvMode[] = new boolean[1];
		thisEmvMode[0] = emvMode;
		dest.writeBooleanArray(thisEmvMode);
		dest.writeInt(state);
		dest.writeInt(startHistorical);
		dest.writeInt(nHistorical);
		dest.writeString(convention.toString());
		dest.writeString(protocol.toString());
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
