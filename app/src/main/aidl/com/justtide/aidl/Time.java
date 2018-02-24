package com.justtide.aidl;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.just.api.UtilFun;

public class Time implements Parcelable{
	private static final String TAG = "SpSysCtrl";
	
	public int tm_sec; /* 秒 – 取值区间为[0,59] */
	public int tm_min; /* 分 - 取值区间为[0,59] */
	public int tm_hour; /* 时 - 取值区间为[0,23] */
	public int tm_mday; /* 一个月中的日期 - 取值区间为[1,31] */
	public int tm_mon; /* 月份（从一月开始，0代表一月） - 取值区间为[0,11] */
	public int tm_year; /* 年份，其值等于实际年份减去1900 */
	public int tm_wday; /* 星期 – 取值区间为[0,6]，其中0代表星期天，1代表星期一，以此类推 */
	public int tm_yday; /* 从每年的1月1日开始的天数 – 取值区间为[0,365]，其中0代表1月1日，1代表1月2日，以此类推 */
	public int tm_isdst; /* 夏令时标识符，实行夏令时的时候，tm_isdst为正。不实行夏令时的时候，tm_isdst为0；不了解情况时，tm_isdst()为负*/

	public static final Creator<Time> CREATOR = new Creator<Time>() {

		@Override
		public Time[] newArray(int size) {
			return new Time[size];
		}

		@Override
		public Time createFromParcel(Parcel source) {
			return new Time(source);
		}
	};

	/**
	 *
	 * @param timeBytes input bytes
	 * @param offset 
	 */
	public Time(byte[] timeBytes, int offset){
		if ((timeBytes.length - offset) < 36){
			return ;
		}
		int cp = offset;
		tm_sec = UtilFun.bytesToInt32(timeBytes, cp);
		cp += 4;
		tm_min = UtilFun.bytesToInt32(timeBytes, cp);
		cp += 4;
		tm_hour = UtilFun.bytesToInt32(timeBytes, cp);
		cp += 4;
		tm_mday = UtilFun.bytesToInt32(timeBytes, cp);
		cp += 4;
		tm_mon = UtilFun.bytesToInt32(timeBytes, cp);
		cp += 4;
		tm_year = UtilFun.bytesToInt32(timeBytes, cp);
		cp += 4;
		tm_wday = UtilFun.bytesToInt32(timeBytes, cp);
		cp += 4;
		tm_yday = UtilFun.bytesToInt32(timeBytes, cp);
		cp += 4;
		tm_isdst = UtilFun.bytesToInt32(timeBytes, cp);
		cp += 4;
	}

	/**
	 *
	 * @param sec second
	 * @param min minute
	 * @param hour hours
	 * @param mday day
	 * @param mon month -1
	 * @param year year - 1900
	 */
	public Time(int sec, int min, int hour, int mday, int mon, int year){
		tm_sec = sec;
		tm_min = min;
		tm_hour = hour;
		tm_mday = mday;
		tm_mon = mon;
		tm_year = year;
		tm_wday = -1;
		tm_yday = -1;
		tm_isdst = -1;
	}

	public Time(Parcel source) {
		tm_sec = source.readInt();
		tm_min = source.readInt();
		tm_hour = source.readInt();
		tm_mday = source.readInt();
		tm_mon = source.readInt();
		tm_year = source.readInt();
		tm_wday = source.readInt();
		tm_yday = source.readInt();
		tm_isdst = source.readInt();
	}
	/**
	 *
	 * @return
	 */
	public byte[] getBytes() {
		byte[] thisByte = new byte[9*4];
		int cp = 0;
		UtilFun.int32ToBytes(thisByte, cp, tm_sec);
		cp += 4;
		UtilFun.int32ToBytes(thisByte, cp, tm_min);
		cp += 4;
		UtilFun.int32ToBytes(thisByte, cp, tm_hour);
		cp += 4;
		UtilFun.int32ToBytes(thisByte, cp, tm_mday);
		cp += 4;
		UtilFun.int32ToBytes(thisByte, cp, tm_mon);
		cp += 4;
		UtilFun.int32ToBytes(thisByte, cp, tm_year);
		cp += 4;
		UtilFun.int32ToBytes(thisByte, cp, tm_wday);
		cp += 4;
		UtilFun.int32ToBytes(thisByte, cp, tm_yday);
		cp += 4;
		UtilFun.int32ToBytes(thisByte, cp, tm_isdst);
		cp += 4;
		return thisByte;
	}

	public void dump(){
		Log.d(TAG, "tm_sec:"   + tm_sec);
		Log.d(TAG, "tm_min:"   + tm_min);
		Log.d(TAG, "tm_hour:"  + tm_hour);
		Log.d(TAG, "tm_mday:"  + tm_mday);
		Log.d(TAG, "tm_mon:"   + (tm_mon + 1));
		Log.d(TAG, "tm_year:"  + (tm_year + 1900));
	}

	public String timeStr(){
		String timeStr = "" + (tm_year + 1900);

		if (tm_mon >= 10){
			timeStr = timeStr + (tm_mon + 1);
		}else{
			timeStr = timeStr + "0" + (tm_mon + 1);
		}

		if (tm_mday >= 10){
			timeStr = timeStr + (tm_mday) + "-";
		}else{
			timeStr = timeStr + "0" + (tm_mday) + "-";
		}

		if (tm_hour >= 10){
			timeStr = timeStr + (tm_hour) + ":";
		}else{
			timeStr = timeStr + "0" + (tm_hour) + ":";
		}

		if (tm_min >= 10){
			timeStr = timeStr + (tm_min) + ":";
		}else{
			timeStr = timeStr + "0" + (tm_min) + ":";
		}

		if (tm_sec >= 10){
			timeStr = timeStr + (tm_sec);
		}else{
			timeStr = timeStr + "0" + (tm_sec);
		}
		return timeStr;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(tm_sec);
		dest.writeInt(tm_min);
		dest.writeInt(tm_hour);
		dest.writeInt(tm_mday);
		dest.writeInt(tm_mon);
		dest.writeInt(tm_year);
		dest.writeInt(tm_wday);
		dest.writeInt(tm_yday);
		dest.writeInt(tm_isdst);
	}

	@Override
	public String toString() {
		return super.toString();
	}
};