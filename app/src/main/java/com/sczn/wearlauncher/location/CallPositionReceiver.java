package com.sczn.wearlauncher.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.sczn.wearlauncher.app.LauncherApp;
import com.sczn.wearlauncher.app.LogUtils;
import com.sczn.wearlauncher.util.NetworkUtils;

public class CallPositionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("com.water.wacthlib.ACTION_CALL_POSITION")){
                mHandler.removeMessages(0);
                if(NetworkUtils.isNetworkAvailable(context)){

                    mHandler.sendEmptyMessageDelayed(0,1000);
                }else{
                    mHandler.sendEmptyMessageDelayed(0,3000);
                }
            }

    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtils.i("CallPositionReceiver","call position upload");
            LocationCallBackHelper.getInstance().startLocationAndUpload(LauncherApp.getAppContext());
        }
    };
}
