package com.sczn.wearlauncher.task;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.widget.TextView;

import com.sczn.wearlauncher.R;
import com.sczn.wearlauncher.app.LogUtils;
import com.sczn.wearlauncher.base.AbsActivity;
import com.sczn.wearlauncher.base.view.MyRecyclerView;
import com.sczn.wearlauncher.socket.WaterSocketManager;
import com.sczn.wearlauncher.socket.command.CommandResultCallback;
import com.sczn.wearlauncher.task.adapter.TaskInfoAdapter;
import com.sczn.wearlauncher.task.bean.TaskCmd;
import com.sczn.wearlauncher.task.bean.TaskInfo;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:任务列表
 * Created by Bingo on 2019/1/29.
 */
public class TaskInfoActivity extends AbsActivity {

    private final String TAG = "TaskInfoActivity";

    private MyRecyclerView mRecyclerView;
    private TextView taskEmpty;
    private TaskInfoAdapter taskInfoAdapter;

    private List<TaskInfo> taskInfoList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskinfo);
        initView();
        initData();
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.rv_taskinfo);
        taskEmpty = findViewById(R.id.task_empty);
        taskInfoAdapter = new TaskInfoAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setEmpty(taskEmpty);

        mRecyclerView.setAdapter(taskInfoAdapter);
    }

    private void initData() {
        taskInfoList = new ArrayList<>();
        taskInfoAdapter.setData(taskInfoList);

        //查询本地的数据显示
        taskInfoList = LitePal.findAll(TaskInfo.class);
        upgradeData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.post(mTaskRunnable);

    }

    Runnable mTaskRunnable = new Runnable() {
        @Override
        public void run() {
            TaskCmd taskCmd = new TaskCmd(new CommandResultCallback() {
                @Override
                public void onSuccess(String baseObtain) {

                    LogUtils.i(TAG,"课程表更新 success");

                    taskInfoList = LitePal.findAll(TaskInfo.class);
                    upgradeData();
                }

                @Override
                public void onFail() {
                    LogUtils.i(TAG,"课程表更新 failed");

                }
            });
            WaterSocketManager.getInstance().send(taskCmd);
        }
    };

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTaskRunnable);
    }

    /**
     * 更新列表数据
     */
    private void upgradeData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                taskInfoAdapter.setData(taskInfoList);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WaterSocketManager.getInstance().cancel();
    }
}
