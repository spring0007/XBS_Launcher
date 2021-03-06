package com.sczn.wearlauncher.socket.command;

import com.sczn.wearlauncher.Config;
import com.sczn.wearlauncher.app.LauncherApp;
import com.sczn.wearlauncher.app.LogUtils;
import com.sczn.wearlauncher.base.util.StringUtils;
import com.sczn.wearlauncher.card.sport.SportHelper;
import com.sczn.wearlauncher.chat.model.WechatGroupInfo;
import com.sczn.wearlauncher.chat.model.WechatMessageInfo;
import com.sczn.wearlauncher.chat.util.MsgAlertPlayer;
import com.sczn.wearlauncher.contact.ContactHelper;
import com.sczn.wearlauncher.friend.ObserverManager;
import com.sczn.wearlauncher.location.LocationCallBackHelper;
import com.sczn.wearlauncher.location.util.FenceHelper;
import com.sczn.wearlauncher.setting.util.AudioManagerHelper;
import com.sczn.wearlauncher.socket.WaterSocketManager;
import com.sczn.wearlauncher.socket.command.obtain.LoginObtain;
import com.sczn.wearlauncher.socket.command.post.UploadStepsCmd;
import com.sczn.wearlauncher.socket.command.remind.IntimateSettingHelper;
import com.sczn.wearlauncher.sp.SPKey;
import com.sczn.wearlauncher.sp.SPUtils;
import com.sczn.wearlauncher.task.util.TaskInfoParse;
import com.sczn.wearlauncher.userinfo.UserInfoUtil;
import com.sczn.wearlauncher.util.CmdSendUtils;
import com.sczn.wearlauncher.util.DialogUtil;
import com.sczn.wearlauncher.util.FileHelper;
import com.sczn.wearlauncher.util.MoreFastEvent;
import com.sczn.wearlauncher.util.SystemPermissionUtil;
import com.sczn.wearlauncher.util.TimeUtil;
import com.toycloud.tcwai.instance.TcCmccWatchDevice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Description:GPRS通信协议
 * Created by Bingo on 2019/5/6.
 */
public class CmdReceive {

    private static final String TAG = "CmdReceive";

    //是否解析语音协议
    private static ByteArrayOutputStream voiceFile;
    private static  boolean isLogin = true;

    /**
     * 解析接收的数据
     *
     * @param result
     * @param buffer
     */
    public static void onResult(String result, byte[] buffer){
        LogUtils.i(TAG, " receive data:" + result);
        try {
            JSONObject jsonObject = null;
            if(!StringUtils.isEmpty(result)){
                jsonObject = new JSONObject(result);
            }
            if(jsonObject == null){
                WaterSocketManager.getInstance().onFail();
                LogUtils.d(TAG, "收到错误数据.数据中没有JSON数据");
                return;
            }
            int status = jsonObject.getInt("status");

            if(status!= ResultCode.CODE_SUCCESS){
                if(status == ResultCode.CODE_DEVICE_NOT_LOGIN ){
                    CmdSendUtils.getHelper().loginDelay(5000);
                }
                WaterSocketManager.getInstance().onFail();
                LogUtils.d(TAG, "收到数据状态失败，status ="+status);
                return;
            }
            int type = jsonObject.getInt("type");

            LogUtils.i(TAG, "收到type<<<<< = " + type);
            switch (type) {
                case CmdCode.HEART: //心跳
                    if(status==ResultCode.CODE_DEVICE_NOT_LOGIN){

                    }
                    LogUtils.i(TAG, "HEART data" );
                    break;
                case CmdCode.LOGIN://登录
                    LogUtils.i(TAG, "LOGIN data" );
                    resolveLogin(jsonObject);
                    break;
                case CmdCode.LOCATION://定位
                    int location_a = jsonObject.getInt("a");
                    LogUtils.i(TAG, "LOCATION data, location_a="+location_a );
                    if(location_a == 2){
                        //请求上传定位
                        JSONObject object = jsonObject.getJSONObject("data");
                        if(object!=null) {
                            String id = object.getString("optUserId");
                            LocationCallBackHelper.getInstance().startLocationAndUpload(LauncherApp.getAppContext());
                        }
                    }else if (location_a == 0){

                    }
                    break;
                case CmdCode.UPLOADSTEPS:  //计步
                    LogUtils.i(TAG, "UPLOADSTEPS data" );
                    int a = jsonObject.getInt("a");
                    if(a==2){
                        //请求上传步数
                        JSONObject object = jsonObject.getJSONObject("data");
                        if(object!=null) {
                            String id = object.getString("optUserId");
                            SportHelper.getHelper().uploadStep(id);
                        }
                    }else if(a==0){
                        //设置默认总步数
                        JSONObject object = jsonObject.getJSONObject("data");
                        if(object!=null) {
                            int stepNumber = object.getInt("stepNumber");
                            UserInfoUtil.setStepTarget(LauncherApp.getAppContext(),stepNumber);
                        }
                    }

                    break;
                case CmdCode.SHAKE:  //摇一摇
                    LogUtils.i(TAG, "SHAKE data" );

                    break;
                case CmdCode.PUSH:  //推送确认通知
                    LogUtils.i(TAG, "PUSH data" );

                    break;
                case CmdCode.LINKMAN:  //通讯录
                    LogUtils.i(TAG, "LINKMAN data" );
                    ContactHelper.getInstance().resolveContactJson(result);
                    break;
                case CmdCode.TASK:  //课程表
                    LogUtils.i(TAG, "TASK data" );
                    TaskInfoParse.registerTask(jsonObject);

                    break;
                case CmdCode.INTIMATE_SETTING:  //贴心设置
                    LogUtils.i(TAG, "INTIMATE_SETTING data" );
                    IntimateSettingHelper.getHelper().setInitMateData(jsonObject);

                    break;
                case CmdCode.FENCE_INFO:  //安全围栏
                    LogUtils.i(TAG, "FENCE_INFO data" );
                    FenceHelper.resolveJson(result);

                    break;
                case CmdCode.DAY_REMIND:  //日程提醒
                    LogUtils.i(TAG, "DAY_REMIND data" );

                    break;
                case CmdCode.PUSH_REISSUE: //推送补发
                    break;
                case CmdCode.SHUTDOWN:  //远程关机
                    LogUtils.i(TAG, "SHUTDOWN data" );
                    SystemPermissionUtil.shutdown(LauncherApp.getAppContext());
                    break;
                case CmdCode.TAKE_PHOTO:  //远程拍照
                    LogUtils.i(TAG, "TAKE_PHOTO data" );

                    break;
                case CmdCode.ONE_WAY_LISTENING:  //单向聆听
                    LogUtils.i(TAG, "ONE_WAY_LISTENING data" );
                    SystemPermissionUtil.setIndividualListening(LauncherApp.getAppContext(),jsonObject);

                    break;
                case CmdCode.SHUTDOWN_NTY:  //手表关机通知
                    LogUtils.i(TAG, "SHUTDOWN_NTY data" );

                    break;
                case CmdCode.FENCE_ALARM:  //安全围栏提醒
                    LogUtils.i(TAG, "FENCE_ALARM data" );

                    break;
                case CmdCode.LOW_POWER:  //手表低电压提醒
                    LogUtils.i(TAG, "LOW_POWER data" );

                    break;
                case CmdCode.SIM_CHANGE:  //SIM卡拔出提醒
                    LogUtils.i(TAG, "SIM_CHANGE data" );

                    break;
                case CmdCode.EMERGENCY_CALL:  //紧急接听提醒
                    LogUtils.i(TAG, "EMERGENCY_CALL data" );

                    break;

                case CmdCode.CHAT_MSG:  //聊天收到消息通知
                    try{
                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                        String groudId = jsonObject1.getString("groupId");
                        sendChatMsg(groudId);
                        LogUtils.i(TAG, "CHAT_MSG data,groudId="+groudId );
                    }catch (Exception e){
                        LogUtils.i(TAG, "CHAT_MSG data error" );
                    }

                    break;
               /* case CmdCode.VERNO:
                    MxyLog.i(TAG, "当前版本号:" + Build.DISPLAY);
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.VERNO + "," + Build.DISPLAY, null));
                    break;
                case CmdCode.CR:
                    //定位
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.CR, null));
                    LocationUtil.getInstance().start(LauncherApp.getAppContext());
                    break;
                case CmdCode.UPLOAD:
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.UPLOAD, null));
                    if (!StringUtils.isEmpty(data)) {
                        MxyLog.i(TAG, "定位间隔:" + data);
                        SPUtils.setParam(SPKey.LOCATION_GPS_CYCLE_KEY, data);
                    }
                    break;
                case CmdCode.PHB2:
                    //收到通讯录
                    MxyLog.d(TAG, "后五个号码");
                    ContactHelper.getInstance().logicGprs(data, 2);
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.PHB2, null));
                    break;
                case CmdCode.PHB:
                    MxyLog.d(TAG, "前五个号码");
                    ContactHelper.getInstance().logicGprs(data, 1);
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.PHB, null));
                    break;
                case CmdCode.MONITOR:
                    //监听
                    doCalling(data);
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.MONITOR, null));
                    break;
                case CmdCode.FIND:
                    //找手表
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.FIND, null));
                    SoundPoolUtil.getInstance().loadResAndPlay(LauncherApp.getAppContext(), R.raw.schedule_sound, 10);
                    DialogUtil.showMsgDialog(LauncherApp.getAppContext(),
                            LauncherApp.getAppContext().getResources().getString(R.string.find),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DialogUtil.dismissMsgDialog();
                                    SoundPoolUtil.release();
                                }
                            });
                    break;
                case CmdCode.POWEROFF:
                    //关机
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.POWEROFF, null));
                    SystemPermissionUtil.shutdown(LauncherApp.getAppContext());
                    break;
                case CmdCode.CENTER:
                    //中心设置
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.CENTER, null));
                    setCenter(data);
                    break;
                case CmdCode.LOWBAT:
                    //低电短信报警开关
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.LOWBAT, null));
                    setLowSwitch(data);
                    break;
                case CmdCode.SOSSMS:
                    //SOS短信报警开关
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.SOSSMS, null));
                    setSosSmsSwitch(data);
                    break;
                case CmdCode.REMOVESMS:
                    //取下手表短信报警开关
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.REMOVESMS, null));
                    setRemoveSmsSwitch(data);
                    break;
                case CmdCode.REMOVE:
                    //取下手环报警开关
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.REMOVE, null));
                    setRemoveSwitch(data);
                    break;
                case CmdCode.PEDO:
                    //计步功能开关
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.PEDO, null));
                    setPedoSwitch(data);
                    break;
                case CmdCode.WALKTIME:
                    //计步时间段设置
                    setWalkTime(data);
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.WALKTIME, null));
                    break;
                case CmdCode.SOS1:
                    setSosNum(data);
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.SOS1, null));
                    break;
                case CmdCode.SOS2:
                    setSosNum(data);
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.SOS2, null));
                    break;
                case CmdCode.SOS3:
                    setSosNum(data);
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.SOS3, null));
                    break;
                case CmdCode.SOS:
                    setSosNum(data);
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.SOS, null));
                    break;
                case CmdCode.IP:
                    //IP端口设置
                    setIpReset(data);
                    break;
                case CmdCode.FACTORY:
                    //恢复出厂设置
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.FACTORY, null));
                    SystemPermissionUtil.reset();
                    break;
                case CmdCode.LZ:
                    //设置语言和时区
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.LZ, null));
                    setLanguageTimeZone(data);
                    break;
                case CmdCode.RESET:
                    //重启
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.RESET, null));
                    SystemPermissionUtil.reboot();
                    break;
                case CmdCode.PW:
                    //控制密码设置
                    //设置终端短信控制密码,非中心号码发送短信指令给终端需添加此密码
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.PW, null));
                    setPw(data);
                    break;
                case CmdCode.CALL:
                    //拨打电话
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.CALL, null));
                    SystemPermissionUtil.doCalling(data);
                    break;
                case CmdCode.SILENCETIME:
                    //免打扰时间段设置
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.SILENCETIME, null));
                    TaskInfoParse.logicGprs(data);
                    break;
                case CmdCode.FLOWER:
                    //小红花个数设置指令
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.FLOWER, null));
                    setFlower(data);
                    break;
                case CmdCode.REMIND:
                    //闹钟设置指令
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.REMIND, null));
                    GprsAlarmClock.set(data);
                    break;
                case CmdCode.MESSAGE:
                    //短语显示设置指令
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.MESSAGE, null));
                    showText(data);
                    break;
                case CmdCode.profile:
                    //情景模式
                    WaterSocketManager.getInstance().send(new StringTo(CmdCode.profile, null));
                    setProfile(data);
                    break;
                case CmdCode.TK:
                    saveVoiceFile(buffer);
                    break;*/
                default:
                    break;
            }
            WaterSocketManager.getInstance().onSuccess(result);
        } catch (JSONException e) {
            WaterSocketManager.getInstance().onFail();
            e.printStackTrace();
        }
    }
//******************************************************************************************

    /**
     *判断状态码
     * @param status
     * @return
     */
    private static boolean judgeStatus(int status){
        /*状态码：
        系统：
        0, "操作成功！"
        1, "操作失败，请重试！"
        2, "系统错误，请重试！"
        3, "您没有权限访问！"
        4, "请求参数非法！"
        设备：
        20, "请求type错误！"
        21, "设备被未注册！"
        22, "设备没有登录！"*/
        if(status == 0)return true;
        return false;
    }


    /**
     * 分解登陆Json数据
     */
    private static void resolveLogin(JSONObject jsonObject) throws JSONException {
        //{"a":1,"data":{"createTime":1584760404000,"dv":"87","imei":"863502040003718",
        // "model":"V5","phone":null,"sd":"65","watchId":25},
        // "message":"登录成功","no":null,"status":0,"timestamp":1584944054,"type":1}
        LoginObtain loginObtain = new LoginObtain();
        JSONObject data = jsonObject.getJSONObject("data");
        loginObtain.setCreateTime(data.getLong("createTime"));
        loginObtain.setDv(data.getString("dv"));
        loginObtain.setImei(data.getString("imei"));
        loginObtain.setModel(data.getString("model"));
        String phone = data.getString("phone");
        if(phone.equals(null)){
            phone = "";
        }
        loginObtain.setPhone(phone);
        String sd = data.getString("sd");
        if(sd.equals(null)){
            sd = "";
        }
        loginObtain.setSd(sd);
        loginObtain.setWatchId(data.getInt("watchId"));
        LogUtils.i(TAG,"loginObtain="+loginObtain.toString());

    }
    private static String  resolveOptUserId( JSONObject jsonObject,int data_a,String dataStr)throws JSONException {
        int a = jsonObject.getInt("a");
        LogUtils.i(TAG, "LOCATION data" );
        if(a == data_a) {
            //请求上传步数
            JSONObject object = jsonObject.getJSONObject("data");
            if (object != null) {
                String id = object.getString(dataStr);
                return id;
            }
        }
        return "";

    }


    //*******************************************************************************************
    /**
     * 解析保存接收语音文件
     *
     * @param buffer
     */
    private static void saveVoiceFile(byte[] buffer) {
        try {
            //接收语音文件
            if (voiceFile == null) {
                voiceFile = new ByteArrayOutputStream();
            }
            String originally = CommandHelper.bytesToHexString(buffer);
            if (originally != null) {
                originally = originally
                        .replace("7d01", "7d")
                        .replace("7d02", "5b")
                        .replace("7d03", "5d")
                        .replace("7d04", "2c")
                        .replace("7d05", "2a");
            }
            byte[] newBuffer = CommandHelper.hexStringToBytes(originally);
            if (newBuffer != null && newBuffer.length > 23) {
                byte[] fileData = new byte[newBuffer.length - 2];
                System.arraycopy(newBuffer, 1, fileData, 0, newBuffer.length - 2);
                voiceFile.write(fileData, 0, fileData.length);
                FileHelper.saveVoiceData(voiceFile.toByteArray(), TimeUtil.getTimeToVoiceName());
                voiceFile.reset();
                voiceFile = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置语言
     * 设置系统时区
     *
     * @param flag
     */
    private static void setLanguageTimeZone(String flag) {
        if (flag.contains(",")) {
            String[] d = flag.split(",");
            if (!StringUtils.isEmpty(d[0])) {
                LogUtils.i(TAG, "language:" + d[0]);
                SystemPermissionUtil.setLanguage(d[0]);
            }
            if (d.length >= 3 && !StringUtils.isEmpty(d[1])) {
                LogUtils.i(TAG, "timeZone:" + d[1]);
                SystemPermissionUtil.setTimeZone(d[1]);
            }
        }
    }

    /**
     * 低电短信报警开关
     *
     * @param data
     */
    private static void setLowSwitch(String data) {
        if (!StringUtils.isEmpty(data)) {
            SPUtils.setParam(SPKey.LOW_SWITCH, data);
        }
    }

    /**
     * SOS短信报警开关
     *
     * @param data
     */
    private static void setSosSmsSwitch(String data) {
        if (!StringUtils.isEmpty(data)) {
            SPUtils.setParam(SPKey.SOS_SMS_SWITCH, data);
        }
    }

    /**
     * 取下手表短信报警开关
     *
     * @param data
     */
    private static void setRemoveSmsSwitch(String data) {
        if (!StringUtils.isEmpty(data)) {
            SPUtils.setParam(SPKey.REMOVE_SMS_SWITCH, data);
        }
    }

    /**
     * 取下手环报警开关
     *
     * @param data
     */
    private static void setRemoveSwitch(String data) {
        if (!StringUtils.isEmpty(data)) {
            SPUtils.setParam(SPKey.REMOVE_SWITCH, data);
        }
    }

    /**
     * 计步功能开关
     *
     * @param data
     */
    private static void setPedoSwitch(String data) {
        if (!StringUtils.isEmpty(data)) {
            SPUtils.setParam(SPKey.PEDO_SWITCH, data);
        }
    }

    /**
     * 计步时间段设置
     *
     * @param data
     */
    private static void setWalkTime(String data) {
        if (!StringUtils.isEmpty(data)) {
            LogUtils.d(TAG, "计步时间段设置:" + data);
        }
    }

    /**
     * 设置情景模式
     *
     * @param data
     */
    private static void setProfile(String data) {
        if (!StringUtils.isEmpty(data)) {
            AudioManagerHelper mAudioManagerHelper = new AudioManagerHelper(LauncherApp.getAppContext());
            if (data.equals("1")) {//表示震动加响铃
                mAudioManagerHelper.ringAndVibrate();
            } else if (data.equals("2")) {//表示响铃
                mAudioManagerHelper.ring();
                //默认把音量开到上一次的音量的保存的数值
                //如果上次是静音则开启响铃模式时默认设置为2
                int volumeLevel = (int) SPUtils.getParam(SPKey.WATCH_VOLUME, 0);
                if (volumeLevel == 0) {
                    volumeLevel = 2;
                }
                mAudioManagerHelper.setVolume(volumeLevel);
            } else if (data.equals("3")) {//表示震动
                mAudioManagerHelper.vibrate();
            } else if (data.equals("4")) {//表示静音
                mAudioManagerHelper.silent();
                mAudioManagerHelper.setVolume(0);
            }
        }
    }

    /**
     * 设置ip地址
     *
     * @param data
     */
    private static void setIpReset(String data) {
        if (data.contains(",")) {
            String[] d = data.split(",");
            if (d.length >= 3 && !StringUtils.isEmpty(d[0]) && !StringUtils.isEmpty(d[1])) {
                SPUtils.setParam(SPKey.IP, d[0]);
                SPUtils.setParam(SPKey.PORT, Integer.parseInt(d[1]));
                WaterSocketManager.getInstance().reInitSocket();
            }
        }
    }

    /**
     * 控制密码设置
     *
     * @param data
     */
    private static void setPw(String data) {
        if (!StringUtils.isEmpty(data)) {
            SPUtils.setParam(SPKey.PW_NUMBER, data);
        }
    }

    /**
     * 小红花个数设置指令
     *
     * @param data
     */
    private static void setFlower(String data) {
        if (!StringUtils.isEmpty(data)) {
            LogUtils.d(TAG, "小红花个数:" + data);
            DialogUtil.showMsgDialog(LauncherApp.getAppContext(), "小红花个数:" + data);
        }
    }

    /**
     * 中心号码设置
     *
     * @param data
     */
    private static void setCenter(String data) {
        if (!StringUtils.isEmpty(data)) {
            LogUtils.d(TAG, "中心号码设置:" + data);
            SPUtils.setParam(SPKey.CENTER_NUMBER, data);
        }
    }


    /**
     * 终端收到该指令后会自动回拨给指令中设置的号码.
     *
     * @param data
     */
    private static void doCalling(String data) {
        if (!StringUtils.isEmpty(data)) {
            SystemPermissionUtil.doCalling(data);
        }
    }

    /**
     * 解析SOS号码
     *
     * @param data
     */
    private static void setSosNum(String data) {
        if (data.contains(",")) {
            String[] d = data.split(",");
            if (d.length >= 3) {
                if (!StringUtils.isEmpty(d[0])) {
                    LogUtils.d(TAG, "SOS1:" + d[0]);
                    SPUtils.setParam(SPKey.SOS_1, d[0]);
                } else {
                    SPUtils.setParam(SPKey.SOS_1, "");
                }
                if (!StringUtils.isEmpty(d[1])) {
                    LogUtils.d(TAG, "SOS2:" + d[1]);
                    SPUtils.setParam(SPKey.SOS_2, d[1]);
                } else {
                    SPUtils.setParam(SPKey.SOS_2, "");
                }
                if (!StringUtils.isEmpty(d[2])) {
                    LogUtils.d(TAG, "SOS3:" + d[2]);
                    SPUtils.setParam(SPKey.SOS_3, d[2]);
                } else {
                    SPUtils.setParam(SPKey.SOS_3, "");
                }
            }
        }
    }


    /**
     * 收到消息
     *
     * @param data
     */
    private static void showText(String data) {
        MsgAlertPlayer.getPlayer().playMsgAlert();
        if (!StringUtils.isEmpty(data)) {
            //更新标记当前组,未读
            WechatGroupInfo group = new WechatGroupInfo();
            group.setMsgReadStatus(1);
            group.updateAll("groupId = ?", "0");

            WechatMessageInfo msg = new WechatMessageInfo();
            msg.setGroupId("0");
            msg.setContent(CommandHelper.hexUnicode2String(data));
            msg.setSenderType(0);
            msg.setSenderId("0");
            msg.setSenderName("监护人");
            msg.setCreateTime(TimeUtil.getTime());
            msg.save();

            ObserverManager.getInstance().notification(ObserverManager.CHAT_MSG_OBSERVER, "0");
        }
    }
    /**
     * 收到消息
     *
     * @param groudId
     */
    private static void sendChatMsg(String groudId) {
        MsgAlertPlayer.getPlayer().playMsgAlert();
        if (!StringUtils.isEmpty(groudId)) {
            //更新标记当前组,未读
            WechatGroupInfo group = new WechatGroupInfo();
            group.setMsgReadStatus(1);
            group.updateAll("groupId = ?", groudId);

            ObserverManager.getInstance().notification(ObserverManager.CHAT_MSG_OBSERVER, groudId);
        }
    }
}
