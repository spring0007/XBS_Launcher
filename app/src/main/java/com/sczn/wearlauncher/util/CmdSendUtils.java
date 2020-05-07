package com.sczn.wearlauncher.util;

import com.sczn.wearlauncher.Config;
import com.sczn.wearlauncher.app.LogUtils;
import com.sczn.wearlauncher.socket.WaterSocketManager;
import com.sczn.wearlauncher.socket.command.CommandResultCallback;
import com.sczn.wearlauncher.socket.command.post.HeatRateCmd;
import com.sczn.wearlauncher.socket.command.post.LoginCmd;

public class CmdSendUtils {

    private final String TAG = "CmdSendUtils";

    private static CmdSendUtils helper;

    /**
     * @return
     */
    public static CmdSendUtils getHelper() {
        if (null == helper) {
            synchronized (CmdSendUtils.class) {
                if (null == helper) {
                    helper = new CmdSendUtils();
                }
            }
        }
        return helper;
    }


    /**
     * 心跳检测
     */
    public void heartbeat(long time) {
        MoreFastEvent.getInstance().event(time, Config.lk_time, new MoreFastEvent.onEventCallBackWithTimeListener() {
            @Override
            public void onCallback(long eventTime) {
                Config.lk_time = eventTime;
                WaterSocketManager.getInstance().send(new HeatRateCmd(new CommandResultCallback() {
                    @Override
                    public void onSuccess(String baseObtain) {
                        LogUtils.i(TAG, " 心跳包检测onSuccess");
                       /* mHanler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                NetWorkReceiver.lk();
                            }
                        }, 2000);*/
                    }

                    @Override
                    public void onFail() {
                        LogUtils.i(TAG, " 心跳包检测onFail");
                    }
                }));
            }
        });
    }


    /**
     * 登陆login 延迟30秒
     */
    public void loginDelay(long time) {
        MoreFastEvent.getInstance().event(time, Config.DEFAULT_NO_LOGIN, new MoreFastEvent.onEventCallBackWithTimeListener() {
            @Override
            public void onCallback(long eventTime) {
                Config.DEFAULT_NO_LOGIN = eventTime;
                WaterSocketManager.getInstance().send(new LoginCmd(new CommandResultCallback() {
                    @Override
                    public void onSuccess(String baseObtain) {
                        LogUtils.i(TAG, "network change login onSuccess1");
                    }

                    @Override
                    public void onFail() {
                        LogUtils.i(TAG, "network change login onFail1");
                    }
                }));

            }
        });
    }

    /**
     * 直接登陆login 无延迟
     */
    public void login() {
        WaterSocketManager.getInstance().send(new LoginCmd(new CommandResultCallback() {
            @Override
            public void onSuccess(String baseObtain) {
                LogUtils.i(TAG, "network change login onSuccess2");
            }

            @Override
            public void onFail() {
                LogUtils.i(TAG, "network change login onFail2");
            }
        }));

    }
}
