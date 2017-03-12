package com.hlk.wbs.espider.lib.voice;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 声音录制<br />
 * 作者：Hsiang Leekwok on 2015/11/09 10:37<br />
 * 邮箱：xiang.l.g@gmail.com<br />
 */
public class VoiceRecorder {

    private MediaRecorder recorder;
    private static VoiceRecorder instance;
    private Context context;
    private String path, name, fullPath;
    private boolean prepared = false, recording = false, autoRecord = false, wannaCancel = false;

    private WakeLock wakeLock = null;

    public static VoiceRecorder getInstance(String path) {
        if (null == instance) {
            synchronized (VoiceRecorder.class) {
                if (null == instance) {
                    instance = new VoiceRecorder(path);
                }
            }
        }
        return instance;
    }

    public static VoiceRecorder getInstance() {
        if (null == instance) {
            synchronized (VoiceRecorder.class) {
                if (null == instance) {
                    instance = new VoiceRecorder();
                }
            }
        }
        return instance;
    }

    public VoiceRecorder() {
    }

    public VoiceRecorder(String path) {
        setPath(path);
    }

    /**
     * 申请锁
     */
    private void initWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getName());
        }
        /** 保持1分钟的锁屏即可 */
        wakeLock.acquire(60000);
    }

    /**
     * 释放锁
     */
    private void releaseWakeLock() {
        if (null != wakeLock && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    /**
     * 设置保存路径
     */
    public VoiceRecorder setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * 设置文件名
     */
    public VoiceRecorder setName(String name) {
        this.name = name;
        return this;
    }

    public VoiceRecorder setContext(Context context) {
        this.context = context;
        return this;
    }

    public String getPath() {
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    public VoiceRecorder setAutoRecord(boolean autoRecord) {
        this.autoRecord = autoRecord;
        return this;
    }

    public boolean isAutoRecord() {
        return autoRecord;
    }

    public static String generateName() {
        return UUID.randomUUID().toString() + ".amr";
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void prepare() {
        if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            throw new IllegalStateException("External storage is not in use.");
        }
        if (TextUtils.isEmpty(path)) {
            throw new IllegalArgumentException("Please set the record file path first.");
        }
        prepared = false;
        recording = false;
        File file = new File(path);
        if (!file.exists()) {
            // 文件不存在时创建目录
            file.mkdirs();
        }
        if (TextUtils.isEmpty(name)) {
            name = generateName();
        }
        file = new File(path + name);
        fullPath = file.getAbsolutePath();

        try {
            recorder = new MediaRecorder();
            // 设置音频源为麦克风
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置音频格式
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            // 设置MediaRecorder录制音频的编码为amr.
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // 设置采样频率
            //recorder.setAudioSamplingRate(8000);
            // 设置声道
            //recorder.setAudioChannels(1);
            // 设置码率
            //recorder.setAudioEncodingBitRate(16);
            // 设置录制好的音频文件保存路径
            recorder.setOutputFile(fullPath);

            recorder.prepare();
            prepared = true;
            if (null != mOnRecordPreparedListener) {
                mOnRecordPreparedListener.onPrepared();
            }
            if (autoRecord) {
                // 自动开始录制
                start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (null != mOnRecordPreparedListener) {
                mOnRecordPreparedListener.onStartFailed();
            }
        }
    }

    /**
     * 开始录制
     */
    public void start() {
        if (null != recorder && prepared && !recording) {
            initWakeLock();
            wannaCancel = false;
            try {
                recorder.start();
                recording = true;
                if (null != mOnRecordPreparedListener) {
                    mOnRecordPreparedListener.onStartRecording();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (null != mOnRecordPreparedListener) {
                    mOnRecordPreparedListener.onStartFailed();
                }
            }
        }
    }

    /**
     * 停止录制
     */
    public void stop() {
        releaseWakeLock();
        if (null != recorder && prepared && recording) {
            try {
                recorder.stop();
                recorder.reset();
                recorder.release();
                prepared = false;
                recording = false;
                recorder = null;
                if (!wannaCancel && null != mOnRecordPreparedListener) {
                    mOnRecordPreparedListener.onRecordComplete(fullPath);
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    /**
     * 取消录制并删除已录制的文件
     */
    public void cancel() {
        wannaCancel = true;
        stop();
        if (!TextUtils.isEmpty(fullPath)) {
            File file = new File(fullPath);
            file.deleteOnExit();
        }
        if (null != mOnRecordPreparedListener) {
            mOnRecordPreparedListener.onCancel();
        }
    }

    /**
     * 获取音量大小
     *
     * @param max 指定最大值
     * @return 返回指定范围内的音量大小级数
     */
    public int getLevel(int max) {
        if (prepared && recording && null != recorder) {
            try {
                // 获取振幅大小：1-32767
                return max * recorder.getMaxAmplitude() / 32768 + 1;
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return 1;
    }

    private OnRecordingListener mOnRecordPreparedListener;

    public VoiceRecorder addOnRecordListener(OnRecordingListener l) {
        mOnRecordPreparedListener = l;
        return this;
    }

    public interface OnRecordingListener {

        void onPrepared();

        void onStartRecording();

        void onStartFailed();

        void onRecordComplete(String fullPath);

        void onCancel();
    }
}
