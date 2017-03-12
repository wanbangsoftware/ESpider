package com.hlk.wbs.espider.lib.voice;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hlk.hlklib.etc.Log;
import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.lib.CorneredButton;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 声音录制按钮
 * 作者：Hsiang Leekwok on 2015/11/09 10:31<br />
 * 邮箱：xiang.l.g@gmail.com<br />
 */
public class VoiceRecordButton extends CorneredButton implements VoiceRecorder.OnRecordingListener {

    private static final String TAG = VoiceRecordButton.class.getSimpleName();
    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANNA_CANCEL = 3;
    private static final int STATE_OVER_LIMIT = 4;
    private static final int DISTANCE_CANCEL = 50;

    private static final int MSG_AUDIO_PREPARED = 0X110;
    private static final int MSG_VOICE_CHANGED = 0X111;
    private static final int MSG_DIALOG_DIMISS = 0X112;
    /**
     * 录音超出最大时间限制
     */
    private static final int MSG_RECORD_OVER_LIMIT = 0x113;

    private static final int TIMER_INTERVAL = 100;
    private static final int MAX_VOLUME = 7;
    private static final int SHORTEST_LENGTH = 800;

    // 参数设置
    private String path;
    // 正在录制标记和是否准备好录制(长按准备录制)
    private boolean recording = false, ready = false;
    private int currentState = 0;
    /**
     * 最短录音时限和最长录音时限
     */
    private int minimumLimit = SHORTEST_LENGTH, maximumLimit = 0;
    /**
     * 默认手动结束录音
     */
    private int finishType = FinishType.MANUAL;
    private long recordedLength = 0;

    private VoiceRecorder recorder;
    private VoiceRecorderDialog dialog;
    private StateHandler handler;
    private Timer mTimer;

    public VoiceRecordButton(Context context) {
        super(context);
        prepareRecord();
    }

    public VoiceRecordButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceRecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttributes(context, attrs, defStyleAttr);
        prepareRecord();
    }

    private void prepareRecord() {
        recorder = VoiceRecorder.getInstance(path).setAutoRecord(false)
                .addOnRecordListener(this).setContext(getContext());
        dialog = new VoiceRecorderDialog(getContext());
        // 长按开始录制
        setOnLongClickListener(mOnLongClickListener);
        changeRecorderState(STATE_NORMAL);
    }

    private void getAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VoiceRecordButton, defStyleAttr, 0);
        minimumLimit = a.getInteger(R.styleable.VoiceRecordButton_minimum_time_limit, SHORTEST_LENGTH);
        maximumLimit = a.getInteger(R.styleable.VoiceRecordButton_maximum_time_limit, 0);
        a.recycle();
    }

    protected static void log(String string) {
        Log.log(TAG, string);
    }

    protected static void log(String format, Object... args) {
        log(format(format, args));
    }

    protected static String format(String format, Object... args) {
        return Log.format(format, args);
    }

    private OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (null == handler)
                handler = new StateHandler(recorder, dialog);
            finishType = FinishType.MANUAL;
            // 每次录音都用不同的文件名
            recorder.setName(VoiceRecorder.generateName());
            ready = true;
            recorder.prepare();
            return false;
        }
    };

    /**
     * 设置录音文件路径，请事先设置好<br />
     * 可以设置文件完整路径或只设置文件夹路径
     */
    public void setPath(String path) {
        this.path = path;
        if (null != path) {
            int i = path.lastIndexOf('.');
            if (i == path.length() - 4) {
                i = path.lastIndexOf('/');
                String p = path.substring(0, i);
                String f = path.substring(i + 1);
                recorder.setPath(p);
                recorder.setName(f);
            } else {
                recorder.setPath(path);
                //recorder.setName(VoiceRecorder.generateName());
            }
        }
    }

    /**
     * 设置录音的最小时间限制
     */
    public void setMinimumLimit(int minimumLimit) {
        this.minimumLimit = minimumLimit;
    }

    /**
     * 获取已设置的最小时间限制
     */
    public int getMinimumLimit() {
        return minimumLimit;
    }

    /**
     * 设置录音最长时间限制
     */
    public void setMaximumLimit(int maximumLimit) {
        this.maximumLimit = maximumLimit;
    }

    /**
     * 获取已设置的最长时间限制
     */
    public int getMaximumLimit() {
        return this.maximumLimit;
    }

    /**
     * 设置录音文件路径，请事先设置好
     */
    public String getPath() {
        return this.path;
    }

    @Override
    public void onPrepared() {
        dialog.show();
        // 开始录音
        recording = true;
        recordedLength = 0;
        recorder.start();

        if (null == mTimer) {
            mTimer = new Timer();
            mTimer.schedule(new RecordingTimerTask(), 0, TIMER_INTERVAL);
        }
    }

    @Override
    public void onStartRecording() {

    }

    @Override
    public void onStartFailed() {
        if (null != mOnRecordFinishedListener) {
            mOnRecordFinishedListener.onRecordFailed();
        }
    }

    @Override
    public void onRecordComplete(String fullPath) {
        if (null != mOnRecordFinishedListener) {
            mOnRecordFinishedListener.onFinished(recordedLength, fullPath);
        }
        if (finishType == FinishType.OVERLIMIT) {
            dialog.recordedTooLong();
            // 如果是超时则延迟关闭对话框
            handler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 2000);
        }
    }

    @Override
    public void onCancel() {

    }

    private class RecordingTimerTask extends TimerTask {

        private boolean stoped = false;

        @Override
        public void run() {
            if (!stoped) {
                if (recording && null != handler) {
                    if (maximumLimit > 0 && recordedLength >= maximumLimit) {
                        // 超过最大录音限制，停止录音并返回录音结果
                        stoped = true;
                        finishType = FinishType.OVERLIMIT;
                        currentState = STATE_OVER_LIMIT;
                        handler.sendEmptyMessage(MSG_RECORD_OVER_LIMIT);
                    } else {
                        recordedLength += TIMER_INTERVAL;
                        handler.sendEmptyMessage(MSG_VOICE_CHANGED);
                    }
                }
            }
        }
    }

    private static class StateHandler extends Handler {

        private WeakReference<VoiceRecorderDialog> dialog;
        private WeakReference<VoiceRecorder> recorder;

        public StateHandler(VoiceRecorder recorder, VoiceRecorderDialog dialog) {
            this.recorder = new WeakReference<>(recorder);
            this.dialog = new WeakReference<>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            VoiceRecorderDialog dialog = this.dialog.get();
            if (null != dialog) {
                switch (msg.what) {
                    case MSG_VOICE_CHANGED:
                        VoiceRecorder recorder = this.recorder.get();
                        if (null != recorder) {
                            dialog.updateVolume(recorder.getLevel(MAX_VOLUME));
                        }
                        break;
                    case MSG_DIALOG_DIMISS:
                        dialog.dismiss();
                        break;
                    case MSG_RECORD_OVER_LIMIT:
                        dialog.recordedTooLong();
                        VoiceRecorder r = this.recorder.get();
                        if (null != r) {
                            // 停止录音
                            r.stop();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                changeRecorderState(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:
                if (recording) {
                    if (wannaCancel(x, y)) {
                        changeRecorderState(STATE_WANNA_CANCEL);
                    } else if (currentState == STATE_RECORDING) {
                        changeRecorderState(STATE_RECORDING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                // 停止音量获取
                if (null != mTimer) {
                    mTimer.cancel();
                    mTimer = null;
                }
                if (!ready) {
                    reset();
                    return super.onTouchEvent(event);
                }
                if (!recording || recordedLength <= minimumLimit) {
                    dialog.recordedTooShort();
                    recorder.cancel();
                    // 延时关闭录音对话框
                    handler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1000);
                } else if (currentState == STATE_RECORDING) {
                    //正常录制结束
                    dialog.dismiss();
                    recorder.stop();
                } else if (currentState == STATE_WANNA_CANCEL) {
                    dialog.dismiss();
                    recorder.cancel();
                }
                reset();
                break;

            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 如果想要取消（超出范围），返回ture
     */
    private boolean wannaCancel(int x, int y) {
        return x < 0 || x > getWidth() || y < -DISTANCE_CANCEL || y > (getHeight() + DISTANCE_CANCEL);
    }

    private void changeRecorderState(int state) {
        if (currentState != state)
            currentState = state;

        switch (currentState) {
            case STATE_NORMAL:
                setText(R.string.hlklib_voice_recorder_button_text_standby);
                break;
            case STATE_RECORDING:
                setText(R.string.hlklib_voice_recorder_button_text_release);
                if (recording) {
                    dialog.recording();
                }
                break;
            case STATE_WANNA_CANCEL:
                // 用户手指已滑动离开按钮区域，表示想要取消录音
                setText(R.string.hlklib_voice_recorder_button_text_release_cancel);
                dialog.wannaCancel();
                break;
        }
    }

    private void reset() {
        ready = false;
        recording = false;
        changeRecorderState(STATE_NORMAL);
    }

    private OnRecordFinishedListener mOnRecordFinishedListener;

    public void addOnRecordFinishedListener(OnRecordFinishedListener l) {
        mOnRecordFinishedListener = l;
    }

    /**
     * 声音录制完毕监听接口
     */
    public interface OnRecordFinishedListener {

        /**
         * 录音设备打开失败
         */
        void onRecordFailed();

        /**
         * 声音录制完毕回调
         *
         * @param length 录音的时间长度,毫秒
         * @param path   录音文件保存路径
         */
        void onFinished(long length, String path);
    }

    /**
     * 录音结束类型
     */
    public static class FinishType {
        /**
         * 手动结束录音
         */
        public static final int MANUAL = 0;
        /**
         * 超过录音最大时间限制时自动结束录音
         */
        public static final int OVERLIMIT = 1;
    }
}
