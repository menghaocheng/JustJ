package com.justtide.justj;

import com.justtide.aidl.PedReader;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

public class JustjService extends Service {

    private static final String TAG = "JustjService";
    private PedReader mAidlCallback = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: startId = " + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy:");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "service on unbind");
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind:");
        return mBinder;
    }

    private final PedReader.Stub mBinder = new PedReader.Stub(){
        @Override
        public String hello(String inStr) throws RemoteException {
            Log.d(TAG, "test: inStr =" + inStr);
            return inStr;
        }

    };
}
