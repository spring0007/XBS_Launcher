package com.sczn.wearlauncher.chat.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sczn.wearlauncher.R;
import com.sczn.wearlauncher.app.LogUtils;
import com.sczn.wearlauncher.chat.util.AudioManager;
import com.sczn.wearlauncher.chat.util.DialogManager;

/**
 * Created by k.liang on 2018/4/19 16:29
 */
public class AudioRecorderButton extends android.support.v7.widget.AppCompatButton implements AudioManager.AudioStateListener {

    private final String TAG = "AudioRecorderButton";
    private Context context;
    //手指滑动 距离
    private static final int DISTANCE_Y_CANCEL = 50;
    //状态
    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_TO_CANCEL = 3;
    //当前状态
    private int mCurState = STATE_NORMAL;
    //已经开始录音
    private boolean isRecording = false;
    private DialogManager mDialogManager;
    private AudioManager mAudioManager;
    private int mTime= 0;
    //是否触发onlongclick
    private boolean mReady;
    private AudioFinishRecorderListener mListener;

    private static final int MSG_AUDIO_PREPARED = 9;
    private static final int MSG_VOICE_CHANGED = 10;
    private static final int MSG_DIALOG_DIMISS = 11;

    public AudioRecorderButton(Context context) {
        this(context, null);
    }

    public AudioRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        mDialogManager = new DialogManager(getContext());
        mAudioManager = new AudioManager(Environment.getExternalStorageDirectory() + "/Download/");
        mAudioManager.setOnAudioStateListener(this);
        //按钮长按 准备录音 包括start
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LogUtils.i(TAG,"onLongClick");
                mReady = true;
                mAudioManager.prepareAudio();
                return false;
            }
        });
    }

    /**
     * 录音完成后的回调
     */
    public interface AudioFinishRecorderListener {
        //时长  和 文件
        void onFinish(int seconds, String filePath);
    }

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener) {
        mListener = listener;
    }

    //获取音量大小的Runnable
    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            if(isRecording){
                    mTime += 1;
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);
            }
        }
    };

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    mHandler.removeMessages(MSG_DIALOG_DIMISS);
                    mDialogManager.showRecordingDialog();
                    isRecording = true;
                    //new Thread(mGetVoiceLevelRunnable).start();
                    mHandler.post(mGetVoiceLevelRunnable);
                    break;
                case MSG_VOICE_CHANGED:
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
                    mHandler.postDelayed(mGetVoiceLevelRunnable,100);
                    break;
                case MSG_DIALOG_DIMISS:
                    LogUtils.i(TAG,"MSG_DIALOG_DIMISS");
                    mHandler.removeMessages(MSG_VOICE_CHANGED);
                    mHandler.removeMessages(MSG_AUDIO_PREPARED);
                    mDialogManager.dimissDialog();
                    break;
            }
            return false;
        }
    });

    @Override
    public void wellPrepared() {
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN://0
                isRecording = true;
                changeState(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE://2
//                if (isRecording) {
//                    //根据想x,y的坐标，判断是否想要取消
//                    if (wantToCancel(x, y)) {
//                        changeState(STATE_WANT_TO_CANCEL);
//                    } else {
//                        changeState(STATE_RECORDING);
//                    }
//                }
                break;
            case MotionEvent.ACTION_UP://1
                //如果longclick 没触发
                if (!mReady) {
                    reset();
                    return super.onTouchEvent(event);
                }
                //触发了onlongclick 没准备好，但是已经prepared 已经start
                //所以消除文件夹
                LogUtils.i(TAG,"isRecording="+isRecording+",mTime="+mTime+",mCurState="+mCurState);
                if (!isRecording || mTime < 10) {//小于1秒
                    LogUtils.i(TAG,"录音时间太短");
                    mDialogManager.tooShort();
                    mAudioManager.cancel();
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1300);
                } else if (mCurState == STATE_RECORDING) {//正常录制结束
                    LogUtils.i(TAG,"录音成功结束");
                    mHandler.removeCallbacks(mGetVoiceLevelRunnable);
                    mHandler.removeMessages(MSG_VOICE_CHANGED);
                    mDialogManager.dimissDialog();
                    mAudioManager.release();
                    if (mListener != null) {
                        mListener.onFinish(mTime*10/100, mAudioManager.getCurrentFilePath());
                    }
                } else if (mCurState == STATE_RECORDING) {
                    LogUtils.i(TAG,"录音开始");
                    mDialogManager.dimissDialog();
                    //release
                    //callbacktoAct
                } else if (mCurState == STATE_WANT_TO_CANCEL) {
                    LogUtils.i(TAG,"取消录音");
                    mDialogManager.dimissDialog();
                    mAudioManager.cancel();
                    //cancel
                }
                reset();
                break;

        }
        return super.onTouchEvent(event);
    }

    /**
     * 恢复状态 标志位
     */
    private void reset() {
        LogUtils.i(TAG,"reset");
        isRecording = false;
        mReady = false;
        changeState(STATE_NORMAL);
        mTime = 0;

    }

    private boolean wantToCancel(int x, int y) {
        //如果左右滑出 button
        if (x < 0 || x > getWidth()) {
            return true;
        }
        //如果上下滑出 button  加上我们自定义的距离
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }
        return false;
    }

    //改变状态
    private void changeState(int state) {
        if (mCurState != state) {
            mCurState = state;
            switch (state) {
                case STATE_NORMAL://1
                    LogUtils.i(TAG,"changeState,STATE_NORMAL");
                    Drawable drawable = context.getResources().getDrawable(R.drawable.temp_voice);
                    // setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                    setBackground(drawable);
                    break;
                case STATE_RECORDING://2
                    LogUtils.i(TAG,"changeState,STATE_RECORDING");
                    Drawable drawable1 = context.getResources().getDrawable(R.drawable.temp_voice_sel);
                    // setCompoundDrawablesWithIntrinsicBounds(null, drawable1, null, null);
                    setBackground(drawable1);
                    if (isRecording) {
                        mDialogManager.recording();
                    }
                    break;
                case STATE_WANT_TO_CANCEL://3
                    LogUtils.i(TAG,"changeState,STATE_WANT_TO_CANCEL");
                    Drawable drawable2 = context.getResources().getDrawable(R.drawable.temp_voice_sel);
                    // setCompoundDrawablesWithIntrinsicBounds(null, drawable2, null, null);
                    setBackground(drawable2);
                    mDialogManager.wantToCancel();
                    break;
            }
        }
    }
}
