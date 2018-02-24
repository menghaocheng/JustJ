package com.justtide.aidl;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

//import com.justtide.aidl.TaskType;

/**
 * @author Administrator
 * @date 2017/10/15
 */

public class Task  implements Parcelable {

    private static final String TAG = "Task";

    private static final String TASK_TYPE                 = "task_type";
    private static final String TASK_ID                   = "task_id";
    private static final String TASK_TIMEOUT              = "task_timeout";
    private static final String TASK_TRIGGER              = "task_trigger";
    //密钥长度不提供对外长度接口
    private static final String TASK_PINBLOCK_PW_LENGTH   = "task_pinblock_pw_length";
    private static final String TASK_PINBLOCK_RANDOM      = "task_pinblock_random";
    private static final String TASK_PINBLOCK_PRICE_STR   = "task_pinblock_pric_str";
    private static final String TASK_PINBLOCK_TITLE       = "task_pinblock_title";

    public static final int TRIGGER_ID = Integer.MAX_VALUE -1;


    /**
     * <p>多线程任务</p>
     * <p>这些任务，无论何时都会添加；其他任务则会判断当前是否有任务在执行</p>
     */
    public static final TaskType[]MULIT_TASK = {
            TaskType.TASK_TYPE_HIDE_PIN_BLOCK,
            TaskType.TASK_TYPE_TRIGGER ,
    };



    private Bundle mBundle = new Bundle();

    /**
     * 基础task
     * @param id 任务id
     * @param type 任务类型
     * @throws Exception 任务类型为空
     */
    public Task(int id,TaskType type) throws Exception {
        this(id,type,null,-1L);
    }



    /**
     * 需要超时时间的task
     * @param id 任务id
     * @param type 任务类型
     * @param timeout 超时时间
     * @throws Exception 任务类型为空
     */
    public Task(int id, TaskType type, long timeout) throws Exception {
        this(id,type,null,timeout);
    }



    /**
     *
     * @param id id
     * @param type  类别
     * @param triggerInfo 触发信息
     */
    public Task(int id, TaskType type, int triggerInfo) throws Exception {
        this(id,type,null,-1l,triggerInfo);
    }



    /**
     * 需要设置迷药长度的task
     * @param id
     * @param type
     * @param pwLength
     */
    public Task(int id, TaskType type, int[] pwLength) throws Exception {
        this(id,type,pwLength,-1l);
    }



    /**
     *
     * @param id id
     * @param type  类别
     * @param pwLength 合法密码长度组
     * @param timeout 超时时间
     * @throws Exception
     */
    public Task(int id, TaskType type, int[] pwLength, long timeout) throws Exception {
        this(id,type,pwLength,timeout,0);
    }



    /**
     *
     * @param id id
     * @param type  类别
     * @param pwLength 合法密码长度组
     * @param timeout 超时时间
     * @param triggerInfo 触发信息
     * @throws Exception
     */
    public Task(int id, TaskType type, int[] pwLength, long timeout, int triggerInfo ) throws Exception {
        this(id,type,pwLength,timeout,triggerInfo,new String[1]);
    }

    /**
     *
     * @param id id
     * @param type  类别
     * @param pwLength 合法密码长度组
     * @param randomKey 随机数
     * @param pricStr 字符串描述的价格
     * @throws Exception
     */
    public Task(int id, TaskType type, int[] pwLength, String[] randomKey, String pricStr) throws Exception {
        this(id,type,pwLength,-1,0,randomKey, pricStr);
    }

    /**
     *
     * @param id id
     * @param type  类别
     * @param pwLength 合法密码长度组
     * @param randomKey 随机数
     * @param title  Pinblock 的title，默认为“金额”
     * @param pricStr 字符串描述的价格
     * @throws Exception
     */
    public Task(int id, TaskType type, int[] pwLength, String[] randomKey, String pricStr, String title) throws Exception {
       this(id,type,pwLength,-1,0,randomKey, pricStr, title);
    }

    /**
     *
     * @param id id
     * @param type  类别
     * @param pwLength 合法密码长度组
     * @param randomKey 随机数
     * @throws Exception
     */
    public Task(int id, TaskType type, int[] pwLength, String[] randomKey ) throws Exception {
        this(id,type,pwLength,-1,0,randomKey);
    }
    /**
     *
     * @param id id
     * @param type  类别
     * @param pwLength 合法密码长度组
     * @param timeout 超时时间
     * @param randomKey 随机数
     * @throws Exception
     */
    public Task(int id, TaskType type, int[] pwLength, long timeout, int triggerInfo, String[] randomKey ) throws Exception {
        if(type== null){
            throw new Exception("type can not be null");
        }

        mBundle.putInt(TASK_ID,id);
        mBundle.putSerializable(TASK_TYPE,type);
        mBundle.putIntArray(TASK_PINBLOCK_PW_LENGTH,pwLength);
        mBundle.putLong(TASK_TIMEOUT,timeout);
        mBundle.putInt(TASK_TRIGGER,triggerInfo);
        mBundle.putStringArray(TASK_PINBLOCK_RANDOM,randomKey);
    }

    /**
     *
     * @param id id
     * @param type  类别
     * @param pwLength 合法密码长度组
     * @param timeout 超时时间
     * @param randomKey 随机数
     * @param pricStr 字符串描述的价格
     * @throws Exception
     */
    public Task(int id, TaskType type, int[] pwLength, long timeout, int triggerInfo, String[] randomKey, String pricStr ) throws Exception {
        if(type== null){
            throw new Exception("type can not be null");
        }

        mBundle.putInt(TASK_ID,id);
        mBundle.putSerializable(TASK_TYPE,type);
        mBundle.putIntArray(TASK_PINBLOCK_PW_LENGTH,pwLength);
        mBundle.putLong(TASK_TIMEOUT,timeout);
        mBundle.putInt(TASK_TRIGGER,triggerInfo);
        mBundle.putStringArray(TASK_PINBLOCK_RANDOM,randomKey);
        mBundle.putString(TASK_PINBLOCK_PRICE_STR,pricStr);
    }

    /**
     *
     * @param id id
     * @param type  类别
     * @param pwLength 合法密码长度组
     * @param timeout 超时时间
     * @param randomKey 随机数
     * @param pricStr 字符串描述的价格
     * @param title Pinblock 的title，默认为“金额”

     * @throws Exception
     */
    public Task(int id, TaskType type, int[] pwLength, long timeout, int triggerInfo, String[] randomKey, String pricStr, String title ) throws Exception {
        if(type== null){
            throw new Exception("type can not be null");
        }

        mBundle.putInt(TASK_ID,id);
        mBundle.putSerializable(TASK_TYPE,type);
        mBundle.putIntArray(TASK_PINBLOCK_PW_LENGTH,pwLength);
        mBundle.putLong(TASK_TIMEOUT,timeout);
        mBundle.putInt(TASK_TRIGGER,triggerInfo);
        mBundle.putStringArray(TASK_PINBLOCK_RANDOM,randomKey);
        mBundle.putString(TASK_PINBLOCK_PRICE_STR,pricStr);
        mBundle.putString(TASK_PINBLOCK_TITLE,title);
    }

    protected Task(Parcel in) {
        mBundle =  in.readBundle();
    }



    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };


    /**
     * 获得task的taskType
     *
     * @return
     */
    public TaskType getTaskType(){
        try {
            TaskType type = (TaskType) mBundle.getSerializable(TASK_TYPE);
            return ((null != type) ? (type) : null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获得task的id
     * @return 如果小于0，则获取失败
     */
    public  int getId(){
        try{
            int id = mBundle.getInt(TASK_ID);
            return id;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获取设置的密钥长度有效值列表
     * @return null：获取失败
     */
    public int[] getPwLength(){
        try{
            int[] length = mBundle.getIntArray(TASK_PINBLOCK_PW_LENGTH);
            return length;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取task的超时时间
     * @return 如果没有设置超时时间或者在获取超时时间的过程中出错，返回-1<br>
     *     其他情况返回设置的超时时间
     */
    public long getTimeout(){
        try {
            long timeout = mBundle.getLong(TASK_TIMEOUT);
            return timeout;
        }catch (Exception e){
            e.printStackTrace();;
            return -1l;
        }
    }

    public int getTrigger(){
        try {
            int trigger = mBundle.getInt(TASK_TRIGGER);
            return trigger;
        }catch (Exception e){
            e.printStackTrace();;
            return 0;
        }
    }

    public String[] getRandomKey(){
        try {
            String[] keys = mBundle.getStringArray(TASK_PINBLOCK_RANDOM);
            return keys;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String getPrice(){
        try {
            String thisPric = mBundle.getString(TASK_PINBLOCK_PRICE_STR);
            return thisPric;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String getTitle(){
        try {
            String thisTitle = mBundle.getString(TASK_PINBLOCK_TITLE);
            return thisTitle;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        try {
            dest.writeBundle(mBundle);
        } catch (Exception e) {
        }
    }



//--------------------------------------------  一些需要公开的信息 --------------------------------------------------------
    /** 连接服务的Conn的Action */
    public static final String ACTION_CONN_SERVICE = "com.justtide.taskserver.justservice.conn";



}

