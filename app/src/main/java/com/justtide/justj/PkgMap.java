package com.justtide.justj;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mc.meng on 2017/9/13.
 */

public class PkgMap {
    Map<Integer, String> mPidPkgNameMap = new HashMap<Integer, String>();

    public PkgMap(){

    }

    public int putPkgName(String pkgName){
        int curPid = android.os.Process.myPid();

        for(Map.Entry<Integer, String> entry : mPidPkgNameMap.entrySet()){
            if (entry.getKey() == curPid){
                entry.setValue(pkgName);
                return 0;
            }
        }

        mPidPkgNameMap.put(curPid, pkgName);
        return 0;
    }


    public String getPkgName(){
        int curPid = android.os.Process.myPid();

        for(Map.Entry<Integer, String> entry : mPidPkgNameMap.entrySet()){
            if (entry.getKey() == curPid){
                return entry.getValue();
            }
        }
        return null;
    }
}
