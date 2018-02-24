package com.justtide.aidl;


import android.os.Parcel;
import android.os.Parcelable;

public final class MagneticCard implements Parcelable {
	
	public static final int TRACK_1 = 0;
	public static final int TRACK_2 = 1;
	public static final int TRACK_3 = 2;

	public static final	int E_ERR_SWIPE = 12;		/*刷卡错误*/
	
	private String[] cardData = {"","",""};
	private int nReaResult = -1;
	private String accountNo = "";
	private String serviceCode = "";
	private String expirationDate = "";
	private String extraData = "";

	public static final Creator<MagneticCard> CREATOR = new Creator<MagneticCard>() {

		@Override
		public MagneticCard[] newArray(int size) {
			return new MagneticCard[size];
		}

		@Override
		public MagneticCard createFromParcel(Parcel source) {
			return new MagneticCard(source);
		}
	};

	public MagneticCard(String[] cardData, int nReaTrackResult){
		this.cardData = cardData;
		this.nReaResult = nReaTrackResult;
		/* Some cards, such as membership cards, are not 
		 * compliance with bank card specification. Parse here will fail.
         */
		//parse();
	}

	public MagneticCard(Parcel source) {
		cardData[0] = source.readString();
		cardData[1] = source.readString();
		cardData[2] = source.readString();
		nReaResult = source.readInt();
		accountNo = source.readString();
		serviceCode = source.readString();
		expirationDate = source.readString();
		extraData = source.readString();

	}
	
	/*private void parse(){
		String strTrack2 = cardData[TRACK_2];
		if(strTrack2.length() != 0){
			// ;<account no>=<expiration date><service code><extra data>? 
			int indexFC = strTrack2.indexOf("=");
			//int indexETX = strTrack2.indexOf("?");
			accountNo = strTrack2.substring(0, indexFC);
			expirationDate = strTrack2.substring(indexFC+1, indexFC+5);
			serviceCode = strTrack2.substring(indexFC+5, indexFC+8);
			extraData = strTrack2.substring(indexFC+8, strTrack2.length());
		}
	}*/
	
	/**
     * Get track1 data.
     * <p>Note that the data has removed start sentinel "%", end sentinel "?" and LRC character.
     * @return the track1 data string
     */
	public String getTrack1(){
		return cardData[TRACK_1];
	}
	
	/**
     * Get track2 data.
     * <p>Note that the data has remove start sentinel ";", end sentinel "?" and LRC character.
     * @return the track2 data string
     */
	public String getTrack2(){
		return cardData[TRACK_2];
	}
	
	/**
     * Get track3 data.
     * <p>Note that the data has remove start sentinel ";", end sentinel "?" and LRC character.
     * @return the track3 data string
     */
	public String getTrack3(){
		return cardData[TRACK_3];
	}
	
	
	
	public int  getReadResult(){
		return nReaResult;
	}
	
	/**
     * Get magnetic card account number.
     *
     * @return account number string
     */
	public String getPAN(){
		//return accountNo;
		String strTrack2 = cardData[TRACK_2];
		if(strTrack2.length() != 0){
			/* ;<account no>=<expiration date><service code><extra data>? */
			int indexFC = strTrack2.indexOf("=");
			accountNo = strTrack2.substring(0, indexFC);
		}
		return accountNo;
	}
	
	/**
     * Get expiration date of the card.
     *
     * @return Expiration date string
     */
	public String getExpirationDate(){
		String strTrack2 = cardData[TRACK_2];
		if(strTrack2.length() != 0){
			/* ;<account no>=<expiration date><service code><extra data>? */
			int indexFC = strTrack2.indexOf("=");
			expirationDate = strTrack2.substring(indexFC+1, indexFC+5);
		}
		return expirationDate;
	}
	
	/**
     * Get service code of the card.
     * 
     * @return customer name string
     */
	public String getServiceCode(){
		String strTrack2 = cardData[TRACK_2];
		if(strTrack2.length() != 0){
			/* ;<account no>=<expiration date><service code><extra data>? */
			int indexFC = strTrack2.indexOf("=");
			serviceCode = strTrack2.substring(indexFC+5, indexFC+8);
		}
		return serviceCode;
	}
	
	/**
     * Get extra data of the card.
     * 
     * @return customer name string
     */
	public String getExtraData(){
		String strTrack2 = cardData[TRACK_2];
		if(strTrack2.length() != 0){
			/* ;<account no>=<expiration date><service code><extra data>? */
			int indexFC = strTrack2.indexOf("=");
			extraData = strTrack2.substring(indexFC+8, strTrack2.length());
		}
		return extraData;
	}

	/**
	 * Get card issuer identification number.
	 * 
	 * @return card IIN string
	 */
	public String getIIN(){
		String strPAN = getPAN();
		/*The first 6 digits of a credit card number are known as 
		 * the Issuer Identification Number (IIN), previously known 
		 * as bank identification number (BIN), issued under the 
		 * ISO/IEC 7812 standard. These identify the institution 
		 * that issued the card to the card holder.
		 * */
		String strIIN = strPAN.substring(0,6);
		return strIIN;
	}
	
	/**
     * Get customer name of the card.
     *
     * @return customer name string
     */
	public String getName(){
		String customerName = "";
		String strTrack1 = cardData[0];
		if(strTrack1.length() != 0){
			int head = strTrack1.indexOf("^");
			int tail = strTrack1.lastIndexOf("^");
			customerName = strTrack1.substring(head+1, tail);
		}
		return customerName;
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
		dest.writeString(cardData[0]);
		dest.writeString(cardData[1]);
		dest.writeString(cardData[2]);
		dest.writeInt(nReaResult);
		dest.writeString(accountNo);
		dest.writeString(serviceCode);
		dest.writeString(expirationDate);
		dest.writeString(extraData);
	}
}
