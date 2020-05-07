package com.sczn.wearlauncher.setting;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.sczn.wearlauncher.R;
import com.sczn.wearlauncher.app.LogUtils;
import com.sczn.wearlauncher.location.LocationCallBackHelper;
import com.sczn.wearlauncher.socket.WaterSocketManager;
import com.sczn.wearlauncher.socket.command.CmdCode;
import com.sczn.wearlauncher.socket.command.CommandResultCallback;
import com.sczn.wearlauncher.socket.command.post.LowBatteryCmd;
import com.sczn.wearlauncher.socket.command.post.NotifyMsgCmd;
import com.sczn.wearlauncher.util.NetworkUtils;

import cn.com.waterworld.alarmclocklib.interfaces.OnSwithChangeListener;
import cn.com.waterworld.alarmclocklib.model.AlarmBean;

public class TestSettingsActivity extends AppCompatActivity implements View.OnClickListener ,CompoundButton.OnCheckedChangeListener {

    //adb shell am start -n com.waterworld.doctor.gprscmd/com.sczn.wearlauncher.setting.TestSettingsActivity
    private static final String TAG ="TestSettingsActivity";
    TextView bootTime,shutDownTime;
    Switch bootEnable,shutDownEnable,volteEnable ,noShutDownEnable,callEnable,powerEnable,saveEnable, positionEnable,answerEnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test_settings);
        findViewById(R.id.test_low_battery).setOnClickListener(this);
        findViewById(R.id.test_shutdown).setOnClickListener(this);
        findViewById(R.id.test_has_sim).setOnClickListener(this);
        bootTime = findViewById(R.id.boot_time);
        shutDownTime = findViewById(R.id.shut_down_time);

        bootEnable = findViewById(R.id.boot_enable);
        shutDownEnable = findViewById(R.id.shut_down_enable);
        volteEnable = findViewById(R.id.volte_enable);
        noShutDownEnable = findViewById(R.id.no_shut_down);
        callEnable = findViewById(R.id.shield_strange_call_enable);
        powerEnable = findViewById(R.id.reserver_power_enable);
        saveEnable =   findViewById(R.id.power_saving_enable);
        positionEnable =  findViewById(R.id.call_position_enable);
        answerEnable = findViewById(R.id.call_answer_enable);

    }

    @Override
    protected void onStart() {
        super.onStart();
        getSystemDefaultValut(this);

        bootEnable.setOnCheckedChangeListener(this);
        shutDownEnable.setOnCheckedChangeListener(this);
        volteEnable.setOnCheckedChangeListener(this);
        noShutDownEnable.setOnCheckedChangeListener(this);
        callEnable.setOnCheckedChangeListener(this);
        powerEnable.setOnCheckedChangeListener(this);
        saveEnable.setOnCheckedChangeListener(this);
        positionEnable.setOnCheckedChangeListener(this);
        answerEnable.setOnCheckedChangeListener(this);

    }

    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param buttonView The compound button view whose state has changed.
     * @param isChecked  The new checked state of buttonView.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.boot_enable:
                putSystemBoolean(getContentResolver(),"default_auto_shut_down",isChecked);//定时关机
                break;
            case R.id.shut_down_enable:
                putSystemBoolean(getContentResolver(),"default_auto_boot",isChecked);//定时开机
                break;
            case R.id.volte_enable:
                putSystemBoolean(getContentResolver(),"default_enable_volte",isChecked);//启用volte高清通话
                enableVoLTE(this,isChecked);
                break;
            case R.id.no_shut_down:
                putSystemBoolean(getContentResolver(),"default_no_shut_down",isChecked);//禁止关机
                break;
            case R.id.shield_strange_call_enable:
                putSystemBoolean(getContentResolver(),"default_shield_strange_call",isChecked);//屏蔽陌生呼叫
                break;
            case R.id.reserver_power_enable:
                putSystemBoolean(getContentResolver(),"default_reserver_power",isChecked);//预留电量
                break;
            case R.id.power_saving_enable:
                putSystemBoolean(getContentResolver(),"default_power_saving",isChecked);//省电模式
                break;
            case R.id.call_position_enable:
                putSystemBoolean(getContentResolver(),"default_call_position",isChecked);//通话位置
                break;
            case R.id.call_answer_enable:
                putSystemBoolean(getContentResolver(),"default_auto_answer",isChecked);//自动接听
                break;
        }

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.test_low_battery:
                lowBattery();
                break;
            case R.id.test_shutdown:
                LocationCallBackHelper.getInstance().startShutDownAndUpload(this);
                break;
            case R.id.test_has_sim:
                hasSIM();
                break;
            case R.id.test_fence:
                break;
        }
    }
    private void lowBattery(){
                LowBatteryCmd lowBatteryCmd = new LowBatteryCmd( new CommandResultCallback() {
                    @Override
                    public void onSuccess(String baseObtain) {
                        LogUtils.d(TAG, "低电量警告onSuccess");
                    }

                    @Override
                    public void onFail() {
                        LogUtils.d(TAG, "低电量警告onFail");
                    }
                });
                WaterSocketManager.getInstance().send(lowBatteryCmd);
    }

    private void hasSIM(){
        boolean hasSIM = NetworkUtils.isHasSimCard(this);
        if (!hasSIM && NetworkUtils.isNetworkAvailable(this)) {
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
private void getSystemDefaultValut(Context mContext){
    ContentResolver contentResolver = mContext.getContentResolver();
    String boot_Time = getSystemString(contentResolver,"default_boot_time" );//开机时间
    bootTime.setText("自动开机时间："+boot_Time);
    String shut_down_time = getSystemString(contentResolver,"default_shut_down_time");//关机时间
    shutDownTime.setText("自动关机时间："+shut_down_time);
    boolean shutDown = getSystemBoolean(contentResolver,"default_auto_shut_down",0);//定时关机
    shutDownEnable.setChecked(shutDown);
    boolean autoboot = getSystemBoolean(contentResolver,"default_auto_boot",0);//定时开机
    bootEnable.setChecked(autoboot);
    boolean volte_enable = getSystemBoolean(contentResolver,"default_enable_volte",0);//启用volte高清通话
    volteEnable.setChecked(volte_enable);
    boolean no_shut_down = getSystemBoolean(contentResolver,"default_no_shut_down",0);//禁止关机
    noShutDownEnable.setChecked(no_shut_down);
    boolean call_enable = getSystemBoolean(contentResolver,"default_shield_strange_call",0);//屏蔽陌生呼叫
    callEnable.setChecked(call_enable);
    boolean reserver_power_enable = getSystemBoolean(contentResolver,"default_reserver_power",0);//预留电量
    powerEnable.setChecked(reserver_power_enable);
    boolean saving_enable = getSystemBoolean(contentResolver,"default_power_saving",0);//省电模式
    saveEnable.setChecked(saving_enable);
    boolean position_enable = getSystemBoolean(contentResolver,"default_call_position",0);//通话位置
    positionEnable.setChecked(position_enable);
    boolean answer_enable =getSystemBoolean(contentResolver,"default_auto_answer",0);//自动接听
    answerEnable.setChecked(answer_enable);
    mContext.sendBroadcast(new Intent("com.water.watch.enableAutoPowerOnorOff")); //自动开关机广播

}
    private String  getSystemString(ContentResolver contentResolver, String name){
        return Settings.System.getString(contentResolver,name);
    }

    private boolean getSystemBoolean( ContentResolver contentResolver,String name ,int value){
       return Settings.System.getInt(contentResolver,name,value)==1;
    }
    private static void putSystemBoolean( ContentResolver contentResolver,String name ,boolean value){
        LogUtils.i(TAG,"putSystemBoolean,name="+name+",value="+value);
        Settings.System.putInt(contentResolver,name,value?1:0);
    }
    /**
     * 开启或者关闭VoLTE。仅当4G模式时，此开关能启作用
     */
    private void enableVoLTE(Context ctx, boolean open) {
        LogUtils.i(TAG, " enableVoLTE. open: " + open);
        Intent enableVoLTEIntent = new Intent("com.water.watch.ACTION_ENABLE_VOLTE");
        enableVoLTEIntent.putExtra("isVolteEnabled", open);
        enableVoLTEIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        ctx.sendBroadcast(enableVoLTEIntent);

    }

    /**
     *  省电模式令通讯模块在高速模式与节能模式间切换。
     *
     * @param ctx
     * @param highSpeed True表示需要打开4G模式，false表示需要切换到非4G模式
     *  //@param netType 0表示断网；1表示2G网络；2表示3G网络；3表示4G网络；
     */
    private void modemEnableHighSpeed(Context ctx, boolean highSpeed/*, int netType*/) {
        android.util.Log.i("lcf_toy", "TcCmccWatchPhone modemEnableHighSpeed. highSpeed: " + highSpeed);
        //FIXME 此接口新增了网络类型参数，需要重新实现；
        //Settings.Global.putInt(ctx.getContentResolver(), EXTRA_SIGNAL_LEVEL_STATUS, highSpeed ? MODEM_STATUS_GOING_HIGH_SPEED : MODEM_STATUS_GOING_POWER_SAVE);
        Intent modemIntent = new Intent("com.water.watchlib.ACTION_MODEM_ENABLE_HIGH_SPEED");
        modemIntent.putExtra("isHighSpeed", highSpeed);
       // modemIntent.putExtra("netType",netType);
        modemIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        ctx.sendBroadcast(modemIntent);
    }


}
