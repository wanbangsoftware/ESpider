package com.hlk.wbs.espider.etc;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * 再按一次退出
 * Created by Hsiang Leekwok on 2015/07/17.
 */
public class PressAgainToExit {

    private static final int PRESS_INTERVAL = 1500;
    private boolean isExit = false;

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            isExit = false;
        }
    };

    /**
     * 执行再次点击退出的过程
     */
    public void doExitInOneSecond() {
        isExit = true;
        HandlerThread thread = new HandlerThread("doTask");
        thread.start();
        new Handler(thread.getLooper()).postDelayed(task, PRESS_INTERVAL);
    }

    /**
     * 查看是否是真的退出
     */
    public boolean isExit() {
        return isExit;
    }

    /**
     * 设置退出标记
     */
    public void setExit(boolean isExit) {
        this.isExit = isExit;
    }
}
