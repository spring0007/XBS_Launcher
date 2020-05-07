package com.sczn.wearlauncher.contact;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.telephony.TelephonyManager;
import android.widget.TextView;

import com.sczn.wearlauncher.R;
import com.sczn.wearlauncher.app.LogUtils;
import com.sczn.wearlauncher.base.AbsActivity;
import com.sczn.wearlauncher.base.view.MyRecyclerView;
import com.sczn.wearlauncher.contact.adapter.ContactsAdapter;
import com.sczn.wearlauncher.contact.impl.OnItemClickListener;
import com.sczn.wearlauncher.socket.WaterSocketManager;
import com.sczn.wearlauncher.socket.command.CommandResultCallback;
import com.sczn.wearlauncher.socket.command.bean.Linkman;
import com.sczn.wearlauncher.util.SystemPermissionUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 通信录
 * Created by Bingo on 2019/1/22.
 */
public class ContactActivity extends AbsActivity {

    private final String TAG = "ContactActivity";

    private MyRecyclerView contactRv;
    private TextView tvNo;
    private ContactsAdapter contactsAdapter;

    private List<Linkman> mDataList;

    //新增加当前的时间戳与上一次的时间
    private static final long CLICK_TIME_INTERVAL = 2 * 1000;
    private long curTimeStamp = 0;
    private long lastTimeStamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        initView();

    }

    private void initView() {
        contactRv = findViewById(R.id.contact_rv);
        tvNo = findViewById(R.id.tv_no);
        contactRv.setLayoutManager(new LinearLayoutManager(this));
        mDataList = new ArrayList<>();

        contactRv.setEmpty(tvNo);
        contactsAdapter = new ContactsAdapter(this);
        contactRv.setAdapter(contactsAdapter);

        contactsAdapter.setData(mDataList);
        contactsAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Linkman bean) {
                lastTimeStamp = curTimeStamp;
                curTimeStamp = System.currentTimeMillis();
                if ((curTimeStamp - lastTimeStamp) > CLICK_TIME_INTERVAL) {
                    String number = bean.getPhone();
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager == null) {
                        showSimNotReady();
                        return;
                    }
                    //water:lqq 这里有问题
                    boolean simReady = telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
                    if (!simReady) {// 没有插入sim卡
                        showSimNotReady();
                    } else {
                        SystemPermissionUtil.doCalling(number);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        mHandler.post(mTaskRunnable);

    }

    Runnable mTaskRunnable = new Runnable() {
        @Override
        public void run() {
            ContactCmd taskCmd = new ContactCmd(new CommandResultCallback() {
                @Override
                public void onSuccess(String baseObtain) {
                    LogUtils.i(TAG,"通讯录更新 success");
                    initData();
                }
                @Override
                public void onFail() {
                    LogUtils.i(TAG,"通讯录更新 failed");
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

    /**
     * 初始化数据
     */
    private void initData() {
        //查询本地的数据显示
        mDataList = LitePal.findAll(Linkman.class);
        upgradeData();
    }

    /**
     * 更新列表数据
     */
    private void upgradeData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contactsAdapter.setData(mDataList);
            }
        });
    }

    /**
     * 提示SIM未准备好
     */
    private void showSimNotReady() {
        showButtonTip(getString(R.string.launcher_main_sim_not_ok), 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
