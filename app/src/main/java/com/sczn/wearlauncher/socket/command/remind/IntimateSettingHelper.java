package com.sczn.wearlauncher.socket.command.remind;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.sczn.wearlauncher.app.LauncherApp;
import com.sczn.wearlauncher.app.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class IntimateSettingHelper {

    private final String TAG = "IntimateSettingHelper";

    private static IntimateSettingHelper helper;

    /**
     * @return
     */
    public static IntimateSettingHelper getHelper() {
        if (null == helper) {
            synchronized (IntimateSettingHelper.class) {
                if (null == helper) {
                    helper = new IntimateSettingHelper();
                }
            }
        }
        return helper;
    }


    public void setInitMateData(JSONObject jsonObject) throws JSONException {

        /*"data":{
            "bootTime":null,	//开机时间
                    "isAutoAnswer":false,	//自动接听
                    "isCallPosition":false,	//通话位置
                    "isPowerSaving":false,	//省电模式
                    "isReservePower":true,	//预留电量
                    "isShieldStrangeCall":false,	//屏蔽陌生呼叫
                    "shutdownTime":null,	//关机时间
                    "isNoShutdown":false	//禁止关机
            "isEnableVolte":false 	//启用volte高清通话
            "isAutoShutdown":false	//定时关机
            "isAutoBoot":false	//定时开机
            "watchId":3
        },*/
        JSONObject object = jsonObject.getJSONObject("data");
        String bootTime = object.getString("bootTime");//开机时间
        String shutdownTime = object.getString("shutdownTime");//关机时间
        boolean isAutoShutdown = object.getBoolean("isAutoShutdown");//定时关机
        boolean isAutoBoot = object.getBoolean("isAutoBoot");//定时开机
        boolean isEnableVolte = object.getBoolean("isEnableVolte");//启用volte高清通话
        boolean isNoShutdown = object.getBoolean("isNoShutdown");//禁止关机
        boolean isShieldStrangeCall = object.getBoolean("isShieldStrangeCall");//屏蔽陌生呼叫
        boolean isReservePower = object.getBoolean("isReservePower");//预留电量
        boolean isPowerSaving = object.getBoolean("isPowerSaving");//省电模式
        boolean isCallPosition = object.getBoolean("isCallPosition");//通话位置
        boolean isAutoAnswer = object.getBoolean("isPowerSaving");//自动接听
        Context mContext =  LauncherApp.getAppContext();
        ContentResolver contentResolver = mContext.getContentResolver();


        putSystemBoolean(contentResolver,"default_no_shut_down",isNoShutdown);//禁止关机
        putSystemBoolean(contentResolver,"default_shield_strange_call",isShieldStrangeCall);//屏蔽陌生呼叫
        putSystemBoolean(contentResolver,"default_reserver_power",isReservePower);//预留电量
        putSystemBoolean(contentResolver,"default_call_position",isCallPosition);//通话位置
        putSystemBoolean(contentResolver,"default_auto_answer",isAutoAnswer);//自动接听

        enableBootTime(mContext,bootTime,isAutoBoot);
        enableShutDownTime(mContext,shutdownTime,isAutoShutdown);

        enableVoLTE(mContext,isEnableVolte);
        modemEnableHighSpeed(mContext,isPowerSaving);
        LogUtils.i(TAG,"贴心设置解析完成");

    }

    private String  getSystemString(ContentResolver contentResolver, String name){
        return Settings.System.getString(contentResolver,name);
    }

    private boolean getSystemBoolean( ContentResolver contentResolver,String name ,int value){
        return Settings.System.getInt(contentResolver,name,value)==1;
    }

    private static void putSystemString(ContentResolver contentResolver, String name, String value){
        Settings.System.putString(contentResolver,name,value);
    }

    private static void putSystemBoolean( ContentResolver contentResolver,String name ,boolean value){
        Settings.System.putInt(contentResolver,name,value?1:0);


    }

    /**
     * 设置定时开机
     */
    private void enableBootTime(Context mContext,String bootTime,boolean isAutoBoot){
        ContentResolver contentResolver = mContext.getContentResolver();
        String default_time = getSystemString(contentResolver,"default_boot_time");
        boolean default_auto = getSystemBoolean(contentResolver,"default_auto_boot",0);
        if(default_time.equals(bootTime) && (default_auto ==isAutoBoot )){

        }else{
            LogUtils.i(TAG,"设置定时开机");
            putSystemString(contentResolver,"default_boot_time",bootTime);//开机时间
            putSystemBoolean(contentResolver,"default_auto_boot",isAutoBoot);//定时开机
            enableAutoPower(mContext,"com.water.watch.enableAutoPowerOn");
           /*  <action android:name="com.water.watch.enableAutoPowerOff"/>
                <action android:name="com.water.watch.enableAutoPowerOn"/>*/
        }

    }

    /**
     * 设置定时关机
     */
    private void enableShutDownTime(Context mContext,String shutdownTime,boolean isAutoShutdown){
        ContentResolver contentResolver = mContext.getContentResolver();

        String default_time = getSystemString(contentResolver,"default_shut_down_time");
        boolean default_auto = getSystemBoolean(contentResolver,"default_auto_shut_down",0);
        if(default_time.equals(shutdownTime) && (default_auto ==isAutoShutdown )){

        }else{
            LogUtils.i(TAG,"设置定时关机");
            putSystemString(contentResolver,"default_shut_down_time",shutdownTime);//关机时间
            putSystemBoolean(contentResolver,"default_auto_shut_down",isAutoShutdown);//定时关机
            enableAutoPower(mContext,"com.water.watch.enableAutoPowerOff");
           /*  <action android:name="com.water.watch.enableAutoPowerOff"/>
                <action android:name="com.water.watch.enableAutoPowerOn"/>*/
        }

    }

    /**
     * 自动开关机广播
     */
    private void enableAutoPower(Context mContext,String action) {
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        mContext.sendBroadcast(intent);
    }

    /**
     * 开启或者关闭VoLTE。仅当4G模式时，此开关能启作用
     */
    private void enableVoLTE(Context ctx, boolean isEnableVolte) {
        boolean default_auto = getSystemBoolean(ctx.getContentResolver(),"default_enable_volte",0);
        if(isEnableVolte!=default_auto) {
            LogUtils.i(TAG, " enableVoLTE. open: " + isEnableVolte);
            putSystemBoolean(ctx.getContentResolver(), "default_enable_volte", isEnableVolte);//启用volte高清通话
            Intent enableVoLTEIntent = new Intent("com.water.watch.ACTION_ENABLE_VOLTE");
            enableVoLTEIntent.putExtra("isVolteEnabled", isEnableVolte);
            enableVoLTEIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            ctx.sendBroadcast(enableVoLTEIntent);
        }
    }

    /**
     *  省电模式令通讯模块在高速模式与节能模式间切换。
     *
     * @param ctx
     * @param highSpeed True表示需要打开4G模式，false表示需要切换到非4G模式
     *  //@param netType 0表示断网；1表示2G网络；2表示3G网络；3表示4G网络；
     */
    private void modemEnableHighSpeed(Context ctx, boolean highSpeed/*, int netType*/) {
        boolean default_auto = getSystemBoolean(ctx.getContentResolver(),"default_power_saving",0);

        if(highSpeed!=default_auto) {
            putSystemBoolean(ctx.getContentResolver(), "default_power_saving", highSpeed);//省电模式
            LogUtils.i(TAG, "modemEnableHighSpeed,highSpeed: " + highSpeed);
            Intent modemIntent = new Intent("com.water.watchlib.ACTION_MODEM_ENABLE_HIGH_SPEED");
            modemIntent.putExtra("isHighSpeed", highSpeed);
            // modemIntent.putExtra("netType",netType);
            modemIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            ctx.sendBroadcast(modemIntent);
        }
    }

}
