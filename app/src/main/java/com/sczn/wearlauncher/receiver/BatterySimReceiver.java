package com.sczn.wearlauncher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.provider.Settings;

import com.sczn.wearlauncher.Config;
import com.sczn.wearlauncher.app.AppConfig;
import com.sczn.wearlauncher.app.LauncherApp;
import com.sczn.wearlauncher.app.LogUtils;
import com.sczn.wearlauncher.base.util.StringUtils;
import com.sczn.wearlauncher.event.RefreshBatteryEvent;
import com.sczn.wearlauncher.location.LocationCallBackHelper;
import com.sczn.wearlauncher.location.bean.LocationInfo;
import com.sczn.wearlauncher.location.impl.OnLocationChangeListener;
import com.sczn.wearlauncher.socket.WaterSocketManager;
import com.sczn.wearlauncher.socket.command.CmdCode;
import com.sczn.wearlauncher.socket.command.CommandResultCallback;
import com.sczn.wearlauncher.socket.command.gprs.post.AL;
import com.sczn.wearlauncher.socket.command.post.NotifyMsgCmd;
import com.sczn.wearlauncher.sp.SPKey;
import com.sczn.wearlauncher.sp.SPUtils;
import com.sczn.wearlauncher.util.MoreFastEvent;
import com.sczn.wearlauncher.util.NetworkUtils;
import com.sczn.wearlauncher.util.SystemPermissionUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * Description:电量状态改变
 * Created by Bingo on 2019/1/17.
 */
public class BatterySimReceiver extends BroadcastReceiver {

    private final String TAG = "BatterySimReceiver";

    // SIM的状态监听
    public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    boolean isAirPlaneOff = true;
    boolean isAirPlaneOn = true;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            LogUtils.d(TAG, "onReceive..");
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                //获取电池的充电状态
                int state = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);

                int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1); //获得当前电量
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1); //获得总电量
                int batteryLevel = 0;
                if (rawLevel >= 0 && scale > 0) {
                    batteryLevel = (int) ((rawLevel * 100f) / scale);
                }
                // 电量变化后，是否在低电量状态
                AppConfig.getInstance().setLowBatteryState(batteryLevel < Config.DEFAULT_LOW_BATTERY);
                AppConfig.getInstance().setBatteryLevel(batteryLevel);
                AppConfig.getInstance().setBatteryState(state);

                LogUtils.d(TAG, "battery_changed:" + rawLevel);
                //更新UI状态
                Config.battery = batteryLevel;
                EventBus.getDefault().post(new RefreshBatteryEvent(batteryLevel, state));
                //预留电量，当电量小于默认值时，开启飞行模式，只能看手表

               if(batteryLevel < Config.DEFAULT_RESERVER_POWER){
                   boolean powerEnable =  Settings.System.getInt(context.getContentResolver(),"default_reserver_power",0)==1;
                    if(powerEnable && isAirPlaneOn) {
                        Intent intentAir = new Intent("com.watchlib.ACTION_AIRPLANE_ON") ;
                        intentAir.putExtra("mode",true);
                        context.sendBroadcast(intentAir);

                        isAirPlaneOn = false;
                        isAirPlaneOff = true;
                    }
               }else{

                   //Settings.Global.getInt(context.getContentResolver(),Settings.Global.AIRPLANE_MODE_ON,0);
                   if( isAirPlaneOff) {
                       Intent intentAir = new Intent("com.watchlib.ACTION_AIRPLANE_ON") ;
                       intentAir.putExtra("mode",false);
                       context.sendBroadcast(intentAir);
                       isAirPlaneOff = false;
                       isAirPlaneOn = true;
                   }

               }
                //低电量提醒 上报服务器  电量值小于默认值，上报服务器
                if (batteryLevel < Config.DEFAULT_LOW_BATTERY && NetworkUtils.isNetworkAvailable(LauncherApp.getAppContext())) {
                    MoreFastEvent.getInstance().event(10 * 60000, Config.upload_battery_last_time, new MoreFastEvent.onEventCallBackWithTimeListener() {
                        @Override
                        public void onCallback(long eventTime) {
                            Config.upload_battery_last_time = eventTime;
                            sendLowBattery();
                        }
                    });
                }
            } else if (ACTION_SIM_STATE_CHANGED.equals(action)) {
                LogUtils.i(TAG, "SIM卡状态改变");
                boolean hasSIM = NetworkUtils.isHasSimCard(context);
                if (!hasSIM && NetworkUtils.isNetworkAvailable(context)) {
                    NotifyMsgCmd notifyMsgCmd = new NotifyMsgCmd(CmdCode.SIM_CHANGE, "", new CommandResultCallback() {
                        @Override
                        public void onSuccess(String baseObtain) {
                            LogUtils.d(TAG, "onSuccess.SIM卡拔出提醒");
                        }

                        @Override
                        public void onFail() {
                            LogUtils.d(TAG, "onFail.SIM卡拔出提醒");
                        }
                    });
                    WaterSocketManager.getInstance().send(notifyMsgCmd);
                }

            }
        }
    }

    /**
     * 向服务器发送,低电量警告
     */
    private void sendLowBattery() {
        /**
         * 判断是否需要低电短信报警短信
         */
        String num = (String) SPUtils.getParam(SPKey.CENTER_NUMBER, "");
        if (!StringUtils.isEmpty(num)) {
            String low_switch = (String) SPUtils.getParam(SPKey.LOW_SWITCH, "0");
            if (!StringUtils.isEmpty(low_switch) && low_switch.equals("1")) {
                SystemPermissionUtil.sendSMS(num, Config.IMEI + "低电短信报警~");
            }
        }
        LocationCallBackHelper.getInstance().startBS(LauncherApp.getAppContext(), new OnLocationChangeListener<LocationInfo>() {
            @Override
            public void success(LocationInfo info) {
                AL al = new AL(info, "00020000", new CommandResultCallback() {
                    @Override
                    public void onSuccess(String baseObtain) {
                        LogUtils.d(TAG, "低电量警告onSuccess");
                    }

                    @Override
                    public void onFail() {
                        LogUtils.d(TAG, "低电量警告onFail");
                    }
                });
                WaterSocketManager.getInstance().send(al);
            }

            @Override
            public void fail() {
                LogUtils.w(TAG, "低电量报警,获取定位信息失败了~");
            }
        });
    }
}
