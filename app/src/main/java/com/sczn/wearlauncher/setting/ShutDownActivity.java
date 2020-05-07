package com.sczn.wearlauncher.setting;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;

import com.sczn.wearlauncher.R;
import com.sczn.wearlauncher.app.LauncherApp;
import com.sczn.wearlauncher.base.AbsActivity;
import com.sczn.wearlauncher.location.LocationCallBackHelper;
import com.sczn.wearlauncher.location.bean.LocationInfo;
import com.sczn.wearlauncher.setting.util.BusinessHelper;
import com.sczn.wearlauncher.socket.WaterSocketManager;
import com.sczn.wearlauncher.socket.WaterSocketService;
import com.sczn.wearlauncher.socket.command.CommandResultCallback;
import com.sczn.wearlauncher.socket.command.post.ShutDownCmd;
import com.sczn.wearlauncher.sp.SPKey;
import com.sczn.wearlauncher.sp.SPUtils;
import com.sczn.wearlauncher.util.AlertInfoDialog;
import com.sczn.wearlauncher.util.SystemPermissionUtil;

/**
 * Description:关机页面
 */
public class ShutDownActivity extends AbsActivity {

    private ImageView mCancelView;
    private ImageView mSureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shut_down);
        initView();
        initListener();

        if (!BusinessHelper.canShutdown(getApplicationContext())) {
            AlertInfoDialog.newInstance()
                    .newDialog(ShutDownActivity.this, getString(R.string.setting_shutdown_not_allow))
                    .setDuration(2 * 1000)
                    .setListener(new AlertInfoDialog.DoListener() {
                        @Override
                        public void onDismiss() {
                            if (!isFinishing()) {
                                finish();
                            }
                        }
                    })
                    .show();
        }
    }

    private void initView() {
        mCancelView = findViewById(R.id.cancel_view);
        mSureView = findViewById(R.id.sure_view);
    }

    private void initListener() {
        mCancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               boolean enable =  Settings.System.getInt(getContentResolver(),"default_no_shut_down",0)==1;//禁止关机
                if (enable) {
                    showButtonTip(getString(R.string.setting_shutdown_not_allow));
                } else {
                    mHandler.sendEmptyMessage(0);
                    showButtonTip(getString(R.string.setting_shutdown_now), 3000);
                }
            }
        });
    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LocationCallBackHelper.getInstance().startShutDownAndUpload(LauncherApp.getAppContext());
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(0);
    }
}
