package com.sczn.wearlauncher.task.util;

import android.provider.Settings;
import android.text.TextUtils;

import com.sczn.wearlauncher.alert.InClassModelDialog;
import com.sczn.wearlauncher.app.LauncherApp;
import com.sczn.wearlauncher.app.LogUtils;
import com.sczn.wearlauncher.socket.command.obtain.TaskObtain;
import com.sczn.wearlauncher.socket.command.obtain.TaskObtain.TimeBean;
import com.sczn.wearlauncher.task.bean.TaskInfo;
import com.sczn.wearlauncher.util.GsonHelper;
import com.sczn.wearlauncher.util.TimeUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Created by Bingo on 2019/3/4.
 */
public class TaskInfoParse {

    private static final String TAG = "TaskInfoParse";

    /**
     * 上课禁用
     *
     * @param data
     */
    public static void logicGprs(String data) {
        if (data.contains(",")) {
            String[] d = data.split(",");
            if (d.length >= 3) {
                LitePal.deleteAll(TaskInfo.class);
                String t1 = d[0];
                String t2 = d[1];
                String t3 = d[2];
                if (t1.contains("-")) {
                    TaskInfo info = new TaskInfo();
                    info.setId(1);
                    info.setStartTime(t1.split("-")[0]);
                    info.setEndTime(t1.split("-")[1]);
                    info.saveOrUpdate("id = ?", "1");
                }
                if (t2.contains("-")) {
                    TaskInfo info = new TaskInfo();
                    info.setId(2);
                    info.setStartTime(t2.split("-")[0]);
                    info.setEndTime(t2.split("-")[1]);
                    info.saveOrUpdate("id = ?", "2");
                }
                if (t3.contains("-")) {
                    TaskInfo info = new TaskInfo();
                    info.setId(3);
                    info.setStartTime(t3.split("-")[0]);
                    info.setEndTime(t3.split("-")[1]);
                    info.saveOrUpdate("id = ?", "3");
                }
            }
        }
        checkIsClassModel(1);
    }


    /**
     * 上课禁用
     *
     * @param jsonObject
     */
    public static void registerTask(JSONObject jsonObject) throws JSONException {
        //"data":{"course":["语文,,,,,体育,,",",数学,,,,,,",",,,体育,语文,,,英语",",,,,,,英语,",",,,,,,,"],"time":[{"startTime":"08:00","endTime":"08:45"},
        // {"startTime":"08:55","endTime":"09:40"},{"startTime":"10:00","endTime":"10:45"},{"startTime":"10:55","endTime":"11:40"},
        // {"startTime":"13:30","endTime":"14:15"},{"startTime":"14:25","endTime":"15:10"},{"startTime":"15:20","endTime":"16:05"},
        // {"startTime":"16:15","endTime":"17:00"}],"isCloseInClass":0},
        JSONObject object = jsonObject.getJSONObject("data");
        if(object==null) return;;
        TaskObtain taskObtain = GsonHelper.getInstance().getGson().fromJson(object.toString(),TaskObtain.class);
        if(taskObtain==null)return;
        LogUtils.i(TAG,"taskObtain="+taskObtain.toString());
        List<TimeBean> timeBeanList = taskObtain.getTime();
        List<String> course= taskObtain.getCourse();
        int isCloseInClass = taskObtain.getCloseInClass();
        if(timeBeanList==null || course==null)return;
        //Settings.System.putInt(LauncherApp.getAppContext().getContentResolver(),"default_classes",course.size());
        Settings.System.putInt(LauncherApp.getAppContext().getContentResolver(),"default_close_inclass",isCloseInClass);
        LitePal.deleteAll(TaskInfo.class);
        List<TaskInfo> infos = new ArrayList<TaskInfo>() ;
        for (int j=0 ;j<course.size();j++){//第几节课
            String week = course.get(j);
            String[] days = week.split(",");
            for(int i = 0;i<days.length;i++){ //第几天
                String classes = days[i];
                if(!TextUtils.isEmpty(classes)){
                    int id = j*8+i+1;
                    TaskInfo taskInfo = new TaskInfo();
                    taskInfo.setId(id);
                    taskInfo.setTask(classes);
                    taskInfo.setStartTime(timeBeanList.get(i).getStartTime());
                    taskInfo.setEndTime(timeBeanList.get(i).getEndTime());
                    taskInfo.setClassDays(j+2);//默认星期一 为 2 , （参考Canlendar ）
                    infos.add(taskInfo);
                    //taskInfo.saveOrUpdate("id = ?", String.valueOf(id));
                }
            }

        }
        LitePal.saveAll(infos);
        checkIsClassModel(isCloseInClass);
    }


    /**
     * 检查上课禁用模式
     *
     * @param isClass
     */
    public static void checkIsClassModel(int isClass) {
        if (isClass == 1) {
            String mHour = TimeUtil.getTimeHour();
            LogUtils.d(TAG, "上课禁用模式." + mHour);
            List<TaskInfo> taskInfoList = LitePal.findAll(TaskInfo.class);
            if (taskInfoList != null && taskInfoList.size() > 0) {
                boolean isShow = false;
                String mClass = "";
                String mStartTime = "";
                String mEndTime = "";
                for (int i = 0; i < taskInfoList.size(); i++) {
                    TaskInfo info = taskInfoList.get(i);
                    if (info.getStartTime() != null && info.getEndTime() != null) {
                        if (TimeUtil.isEffectiveDate(info.getClassDays(), info.getStartTime(), info.getEndTime())) {
                            mClass = info.getTask();
                            mStartTime = info.getStartTime();
                            mEndTime = info.getEndTime();
                            isShow = true;
                            break;
                        }
                    }
                }
                if (isShow) {
                    InClassModelDialog.getInstance().showMsgDialog(LauncherApp.getAppContext(), mClass, mStartTime, mEndTime);
                    LogUtils.w(TAG, "执行上课禁用模式");
                } else {
                    LogUtils.d(TAG, "解除上课禁用模式");
                    InClassModelDialog.getInstance().dismissMsgDialog();
                }
            }
        } else {
            LogUtils.d(TAG, "解除上课禁用模式");
            InClassModelDialog.getInstance().dismissMsgDialog();
        }
    }
}
