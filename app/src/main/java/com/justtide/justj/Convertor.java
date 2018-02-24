package com.justtide.justj;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;


/**
 * Created by mc.meng on 2017/7/21.
 */

public class Convertor extends Thread{
    private static final String TAG = "ThermalPrinter";
    static public final boolean dbg = false;
    static public final int CONVERT_STATUS_INIT = 0;
    static public final int CONVERT_STATUS_ING = 1;
    static public final int CONVERT_STATUS_END = 2;

    int mWorkingFlag = CONVERT_STATUS_INIT;
    private Bitmap mBitmap = null;
    public byte[] mConvertedBuff = null;
    private int writePoint = 0;

    public Convertor(Bitmap bitmap){
        mBitmap =  com.justtide.justtide.Convertor.imageShrink(bitmap);
        mConvertedBuff = new byte[mBitmap.getHeight()*48];
    }

    public static char binaryzationPixel(int pixelColor)
    {
        //int alpha = (pixelColor & 0xff000000) >> 24;
        int red = 	Color.red(pixelColor);//(pixelColor & 0x00ff0000) >> 16;
        int green = Color.green(pixelColor);//(pixelColor & 0x0000ff00) >> 8;
        int blue = Color.blue(pixelColor);//(pixelColor & 0x000000ff) >> 0;

        /* use formula to convert RGB to gray : x = 0.3R + 0.59G + 0.11B */
        int gray = (30*red + 59*green + 11*blue + 50) / 100;
        if(gray <= 160)
            return 1;
        else
            return 0;
    }

    public static byte[] getRowByte(Bitmap bitmap, int row)
    {
        int width = bitmap.getWidth();
        int widthBytes = (width + 7) / 8;
        byte[] rowBytes = new byte[widthBytes];
        int i,j;
        char oneByte = 0;
        char oneBit = 0;
        for(i=0; i < widthBytes; i++)
        {
            for(j=0;j<8;j++)
            {
                oneByte <<= 1;
                if((i*8+j) < width)
                    oneBit = binaryzationPixel(bitmap.getPixel(i*8+j,row));
                else
                    oneBit = 0;
                oneByte += oneBit;
            }
            rowBytes[i] = (byte)(oneByte & 0xff);
        }
        return rowBytes;
    }

    public byte[] _getRowByte(int row)
    {
        int width = mBitmap.getWidth();
        int widthBytes = (width + 7) / 8;
        byte[] rowBytes = new byte[widthBytes];
        int i,j;
        char oneByte = 0;
        char oneBit = 0;
        for(i=0; i < widthBytes; i++)
        {
            for(j=0;j<8;j++)
            {
                oneByte <<= 1;
                if((i*8+j) < width)
                    oneBit = binaryzationPixel(mBitmap.getPixel(i*8+j,row));
                else
                    oneBit = 0;
                oneByte += oneBit;
            }
            rowBytes[i] = (byte)(oneByte & 0xff);
        }
        return rowBytes;
    }


    public static byte[] convert2PrintBuffer(Bitmap bitmap)
    {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int widthByte = (width + 7) / 8;
        byte[] oneRowByte;
        byte[] printBuffer = new byte[height*48];

        for(int i=0;i<height;i++)
        {
            oneRowByte = getRowByte(bitmap,i);
            System.arraycopy(oneRowByte, 0, printBuffer, i*widthByte, widthByte);
        }
        return printBuffer;
    }

    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,double newHeight) {
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

    /*
    public static Bitmap imageShrink(String path){
        Bitmap shrinkBmp,bitmapTemp;
        BitmapFactory.Options options = new BitmapFactory.Options();
        bitmapTemp = BitmapFactory.decodeFile(path,options);

        int height = options.outHeight;
        int width = options.outWidth;
        shrinkBmp = bitmapTemp;

        // is the width exceed 384
        if(width > 384)
        {
            shrinkBmp = zoomImage(bitmapTemp, 384 ,height*384/ width);
        }

        return shrinkBmp;
    }
    */

    public static Bitmap imageShrink(Bitmap bitmap){
        Bitmap shrinkBmp;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        shrinkBmp = bitmap;

        /* is the width exceed 384 */
        if(width > 384)
        {
            shrinkBmp = zoomImage(bitmap, 384 ,height*384/ width);
        }

        return shrinkBmp;
    }

    public boolean ready(){
        return writePoint > (mConvertedBuff.length /2);
    }

    public void run(){
        Log.e(TAG, "run: start to convert");
        mWorkingFlag = CONVERT_STATUS_ING;
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        int widthByte = (width + 7) / 8;

        mConvertedBuff = new byte[height*48];

        int i,j,row;
        char oneByte = 0;
        char oneBit = 0;
        writePoint = 0;
        for(row = 0; row<height; row++)
        {
            for(i=0; i < widthByte; i++)
            {
                for(j=0;j<8;j++)
                {
                    oneByte <<= 1;
                    if((i*8+j) < width)
                        oneBit = binaryzationPixel(mBitmap.getPixel(i*8+j,row));
                    else
                        oneBit = 0;
                    oneByte += oneBit;
                }
                mConvertedBuff[writePoint++ ] = (byte)(oneByte & 0xff);
            }

        }
        mWorkingFlag = CONVERT_STATUS_END;
        mBitmap = null;
    }

    public void runa() {
        Log.e(TAG, "run: start to convert");
        mWorkingFlag = CONVERT_STATUS_ING;
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        int widthByte = (width + 7) / 8;
        byte[] oneRowByte;


        int i = 0;
        writePoint = 0;

        int minBitmapHeight = 0;

        while(true){
            minBitmapHeight = height;

            if (minBitmapHeight > 640){
                minBitmapHeight = 640;
            }
            Bitmap thisBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, minBitmapHeight);

            for(i = 0; i<minBitmapHeight; i++)
            {
                oneRowByte = getRowByte(thisBitmap,i);
                System.arraycopy(oneRowByte, 0, mConvertedBuff, writePoint, widthByte);
                writePoint += widthByte;
            }

            height -= minBitmapHeight;
            if (height <= 0){
                break;
            }
            //break;
        }
        writePoint += widthByte;
        mWorkingFlag = CONVERT_STATUS_END;
        mBitmap = null;
    }

    public void runb() {
            Log.e(TAG, "run: start to convert");
            mWorkingFlag = CONVERT_STATUS_ING;
            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();
            int widthByte = (width + 7) / 8;
            byte[] oneRowByte;
            mConvertedBuff = new byte[height*48];

            int i = 0;
            writePoint = 0;
            for(i = 0; i<height; i++)
            {
                oneRowByte = _getRowByte(i);
                System.arraycopy(oneRowByte, 0, mConvertedBuff, writePoint, widthByte);
                writePoint += widthByte;
            }
            writePoint += widthByte;

            mWorkingFlag = CONVERT_STATUS_END;
            mBitmap = null;
        }



    public int getTotalLen(){
        if (mBitmap == null){
            return 0;
        }
        return mBitmap.getHeight()*48;
    }

    public byte[] getPrintByte(int offset, int len){
        if (dbg) Log.e(TAG, "getPrintByte(" + offset + "," + len + ")writePoint="+writePoint);
        if (mConvertedBuff == null){
            if (dbg)Log.e(TAG, "getPrintByte: mConvertedBuff == null");
            return null;
        }

        if (offset + len > writePoint ){
            if(mWorkingFlag == CONVERT_STATUS_ING) {
                if (dbg)Log.e(TAG, "getPrintByte: offset + len > writePoint, " + writePoint);
                return null;
            }
            else{
                len = writePoint - offset;
            }
        }
        byte[] thisOutBuff = new byte[len];
        System.arraycopy(mConvertedBuff, offset, thisOutBuff, 0, len);
        if (offset + len == writePoint &&  mWorkingFlag == CONVERT_STATUS_END){
            mConvertedBuff = null;
        }
        return thisOutBuff;
    }

    public boolean isFinished(){

        return true;
    }


}
