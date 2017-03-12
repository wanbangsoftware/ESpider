package com.hlk.wbs.espider.lib.voice;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hlk.wbs.espider.R;

/**
 * 作者：Hsiang Leekwok on 2015/11/09 12:35<br />
 * 邮箱：xiang.l.g@gmail.com<br />
 */
public class VoiceRecorderDialog {

    private Context context;
    private Dialog dialog;

    // UI
    private ImageView mMicrophone, mVolume;
    private TextView mWarningText;

    // Volume
    private int[] volumes;

    public VoiceRecorderDialog(Context context) {
        this.context = context;
        volumes = new int[]{
                R.drawable.hlklib_voice_recorder_volume_1,
                R.drawable.hlklib_voice_recorder_volume_2,
                R.drawable.hlklib_voice_recorder_volume_3,
                R.drawable.hlklib_voice_recorder_volume_4,
                R.drawable.hlklib_voice_recorder_volume_5,
                R.drawable.hlklib_voice_recorder_volume_6,
                R.drawable.hlklib_voice_recorder_volume_7
        };
    }

    /**
     * 显示录音对话框
     */
    public void show() {
        if (null == dialog) {
            dialog = new Dialog(context, R.style.HLKLIB_Voice_Recorder_Dialog);
            View view = View.inflate(context, R.layout.hlklib_voice_recorder_dialog, null);
            mMicrophone = (ImageView) view.findViewById(R.id.hlklib_voice_recorder_dialog_mic);
            mVolume = (ImageView) view.findViewById(R.id.hlklib_voice_recorder_dialog_volume);
            mWarningText = (TextView) view.findViewById(R.id.hlklib_voice_recorder_warning);
            dialog.setContentView(view);
        }
        dialog.show();
    }

    /**
     * 关闭录音对话框
     */
    public void dismiss() {
        if (null != dialog && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    /**
     * 开始录制
     */
    public void recording() {
        if (null != dialog && dialog.isShowing()) {
            mMicrophone.setImageResource(R.drawable.hlklib_voice_recorder_microphone);
            mVolume.setVisibility(View.VISIBLE);
            mWarningText.setText(R.string.hlklib_voice_recorder_warning_text_cancel);
        }
    }

    public void wannaCancel() {
        if (null != dialog && dialog.isShowing()) {
            mMicrophone.setImageResource(R.drawable.hlklib_voice_recorder_canceled);
            mVolume.setVisibility(View.GONE);
            mWarningText.setText(R.string.hlklib_voice_recorder_warning_text_wanna_cancel);
        }
    }

    /**
     * 录音时间太短
     */
    public void recordedTooShort() {
        if (null != dialog && dialog.isShowing()) {
            mMicrophone.setImageResource(R.drawable.hlklib_voice_recorder_voice_to_short);
            mVolume.setVisibility(View.GONE);
            mWarningText.setText(R.string.hlklib_voice_recorder_warning_text_too_short);
        }
    }

    /**
     * 录音时间太长
     */
    public void recordedTooLong() {
        if (null != dialog && dialog.isShowing()) {
            mMicrophone.setImageResource(R.drawable.hlklib_voice_recorder_voice_to_short);
            mVolume.setVisibility(View.GONE);
            mWarningText.setText(R.string.hlklib_voice_recorder_warning_text_too_long);
        }
    }

    /**
     * 更新音量大小
     */
    public void updateVolume(int volume) {
        if (null != dialog && dialog.isShowing()) {
            mVolume.setImageResource(volumes[volume - 1]);
        }
    }
}
