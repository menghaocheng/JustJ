package com.justtide.aidl;

public class UtilFun {
	/*
	public static String byteToString(byte[] inByte, int offset, int length) {
		byte[] outBuff = new byte[length];
		
		System.arraycopy(inByte, offset, outBuff, 0, length);
		
		String outStr = outBuff.toString();
		
		return outStr;
    }
	*/

	public static void msleep(int timeMs){
		try {
			Thread.sleep(timeMs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String byteToString(byte[]inBytes, int offset, int length)
	{
		StringBuffer buf = new StringBuffer();
		for(int i=offset;i<offset + length;i++)
		{
			if(inBytes[i] == 0) break;
			buf.append((char) inBytes[i]);
		}
		return buf.toString();
	}
	
	public static void clearByte(byte[] inByte){
		if (inByte == null){
			return ;
		}
		int len = inByte.length;
		for (int i = 0;i < len; i++){
			inByte[i] = 0;
		}

	}
	public static int bytesToInt32(byte[] src, int offset) {  
	    int value;
		if (src.length - offset <= 0){
			return 0;
		}
	    value = (int) ((src[offset] & 0xFF)   
	            | ((src[offset+1] & 0xFF)<<8)   
	            | ((src[offset+2] & 0xFF)<<16)   
	            | ((src[offset+3] & 0xFF)<<24));  
	    return value;
	} 
	
	public static void int32ToBytes(byte[] dst, int offset, int value )   
	{   
	    dst[3+offset] =  (byte) ((value>>24) & 0xFF);  
	    dst[2+offset] =  (byte) ((value>>16) & 0xFF);  
	    dst[1+offset] =  (byte) ((value>>8 ) & 0xFF);    
	    dst[0+offset] =  (byte) ((value>>0 ) & 0xFF);                  
	}

	public static void int16ToBytes(byte[] dst, int offset, int value )
	{
		dst[1+offset] =  (byte) ((value>>8 ) & 0xFF);
		dst[0+offset] =  (byte) ((value>>0 ) & 0xFF);
	}
	
	public static int bytesToInt16(byte[] src, int offset) {  
	    int value;    
	    value = (int) ((src[offset] & 0xFF)   
	            | ((src[offset+1] & 0xFF)<<8));  
	    return value;  
	} 
	
	public static void Int16ToBytes(byte[] dst, int offset, int value )   
	{
	    dst[1+offset] =  (byte) ((value>>8 ) & 0xFF);    
	    dst[0+offset] =  (byte) ((value>>0 ) & 0xFF);                  
	}
	
	/**
	 * 字符串转换成十六进制字符串
	 * 
	 * @param String
	 *            str 待转换的ASCII字符串
	 * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
	 */
	public static String str2HexStr(String str) {

		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder("");
		byte[] bs = str.getBytes();
		int bit;

		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i] & 0x0f;
			sb.append(chars[bit]);
			sb.append(' ');
		}
		return sb.toString().trim();
	}

	/**
	 * 十六进制转换字符串
	 * 
	 * @param String
	 *            str Byte字符串(Byte之间无分隔符 如:[616C6B])
	 * @return String 对应的字符串
	 */
	public static String hexStr2Str(String hexStr) {
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;

		for (int i = 0; i < bytes.length; i++) {
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		return new String(bytes);
	}


	public static String hexToString(byte[] arg, int length)
	{
		String str="", strTemp="";
		int temp;
		for(int i=0;i<length;i++)
		{
			temp = (int)arg[i] & 0xff;
			if(temp <= 0xf){
				strTemp = "0";
				strTemp += Integer.toHexString(arg[i] & 0xff);
			}else{
				strTemp = Integer.toHexString(arg[i] & 0xff);
			}

			str = str+strTemp;
		}
		return str;
	}

	/**
	 * bytes转换成十六进制字符串
	 * 
	 * @param byte[] b byte数组
	 * @return String 每个Byte值之间空格分隔
	 */
	public static String byte2HexStr(byte[] b) {
		String stmp = "";
		StringBuilder sb = new StringBuilder("");
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
			sb.append(" ");
		}
		return sb.toString().toUpperCase().trim();
	}


	/**
	 * bytes字符串转换为Byte值
	 * 
	 * @param String
	 *            src Byte字符串，每个Byte之间没有分隔符
	 * @return byte[]
	 */
	public static byte[] hexStr2Bytes(String src) {
		int m = 0, n = 0;
		int l = src.length() / 2;
		byte[] ret = new byte[l];
		for (int i = 0; i < l; i++) {
			m = i * 2 + 1;
			n = m + 1;
			ret[i] = Byte.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
		}
		return ret;
	}

	/**
	 * String的字符串转换成unicode的String
	 * 
	 * @param String
	 *            strText 全角字符串
	 * @return String 每个unicode之间无分隔符
	 * @throws Exception
	 */
	public static String strToUnicode(String strText) throws Exception {
		char c;
		StringBuilder str = new StringBuilder();
		int intAsc;
		String strHex;
		for (int i = 0; i < strText.length(); i++) {
			c = strText.charAt(i);
			intAsc = (int) c;
			strHex = Integer.toHexString(intAsc);
			if (intAsc > 128)
				str.append("\\u" + strHex);
			else
				// 低位在前面补00
				str.append("\\u00" + strHex);
		}
		return str.toString();
	}

	/**
	 * unicode的String转换成String的字符串
	 * 
	 * @param String
	 *            hex 16进制值字符串 （一个unicode为2byte）
	 * @return String 全角字符串
	 */
	public static String unicodeToString(String hex) {
		int t = hex.length() / 6;
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < t; i++) {
			String s = hex.substring(i * 6, (i + 1) * 6);
			// 高位需要补上00再转
			String s1 = s.substring(2, 4) + "00";
			// 低位直接转
			String s2 = s.substring(4);
			// 将16进制的string转为int
			int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
			// 将int转换为字符
			char[] chars = Character.toChars(n);
			str.append(new String(chars));
		}
		return str.toString();
	}

	public static int[] getIntArrayFromByte(byte[] inByte){
		String thisStr = UtilFun.byteToString(inByte, 0, inByte.length);
		String[] thisStrs = thisStr.split(",");
		int arraySize = thisStrs.length;
		if (arraySize == 0){
			return null;
		}
		int[] thisArray = new int[arraySize];
		for (int i = 0; i < arraySize; i ++){
			int thisLen = Integer.parseInt(thisStrs[i]);
			thisArray[i] = thisLen;
		}

		return thisArray;
	}

	/**
	 * t是否包含在tArray中
	 * @param tArray
	 * @param t
	 * @param <T>
	 * @return
	 */
	public static <T> boolean contains(T[] tArray ,T t){
		for (int i = 0; i < tArray.length; i++) {
			if(tArray[i].equals(t)){
				return true;
			}
		}
		return false;
	}

	/**
	 * t是否包含在tArray中
	 * @param tArray
	 * @param t
	 * @return
	 */
	public static  boolean contains(int[] tArray ,int t){

		if(tArray==null){
			return false;
		}

		for (int i = 0; i < tArray.length; i++) {
			if(tArray[i]==t){
				return true;
			}
		}
		return false;
	}

    static public String[] bytesToString(byte[] inByte) {
        String[] realKeys = new String[inByte.length];
        for (int i=0;i<inByte.length;i++){
            int b = inByte[i];

            if (b == 0) {
                realKeys[i] = "0";
            } else if (b == 1) {
                realKeys[i] = "1";
            } else if (b == 2) {
                realKeys[i] = "2";
            } else if (b == 3) {
                realKeys[i] = "3";
            } else if (b == 4) {
                realKeys[i] = "4";
            } else if (b == 5) {
                realKeys[i] = "5";
            } else if (b == 6) {
                realKeys[i] = "6";
            } else if (b == 7) {
                realKeys[i] = "7";
            } else if (b == 8) {
                realKeys[i] = "8";
            } else if (b == 9) {
                realKeys[i] = "9";
            }
        }
        return realKeys;

    }
}
