package com.sczn.wearlauncher.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.os.Bundle;
import android.view.View;

import com.sczn.wearlauncher.R;

public class SettingMoreActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_more);
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.call_phone).setOnClickListener(this);
        findViewById(R.id.go_settings).setOnClickListener(this);
        findViewById(R.id.engineering_mode).setOnClickListener(this);
        findViewById(R.id.factory_mode).setOnClickListener(this);
        findViewById(R.id.test_mode).setOnClickListener(this);

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.test_mode:
                intent.setClass(getApplicationContext(),TestSettingsActivity.class);
                break;
            case R.id.call_phone:
                intent.setAction(Intent.ACTION_CALL_BUTTON);
                break;
            case R.id.go_settings:
                intent.setAction(Settings.ACTION_SETTINGS);
                break;
            case R.id.engineering_mode:
                intent.setClassName("com.sprd.engineermode","com.sprd.engineermode.EngineerModeActivity");
                break;
            case R.id.factory_mode:
                intent.setClassName("com.sprd.validationtools","com.sprd.validationtools.ValidationToolsMainActivity");
               break;

        }
        startActivity(intent);
    }
}
