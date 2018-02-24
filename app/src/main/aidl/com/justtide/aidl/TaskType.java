package com.justtide.aidl;

/**
 * @author Rhett
 * @date 2017/7/3
 */

public enum TaskType {

    /**
     * pin block
     */
    TASK_TYPE_PIN_BLOCK,


    /**
     * 隐藏已经显示的pinblock
     * @author Rhett
     * @date 2017-7-16
     */
    TASK_TYPE_HIDE_PIN_BLOCK,

    /**
     * 格式化fs
     * <p>耗时操作,设置超时时间60s以上</p>
     */
    TASK_TYPE_FS_FORMAT,

    /**
     * 格式化ped
     * <p>耗时操作,设置超时时间60s以上</p>
     */
    TASK_TYPE_PED_FORMAT,


    /**
     * 显示触发信息
     * <p>需要传递来一个int</p>
     */
    TASK_TYPE_TRIGGER,


}
