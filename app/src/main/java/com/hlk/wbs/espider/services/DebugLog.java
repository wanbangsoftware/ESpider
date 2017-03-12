package com.hlk.wbs.espider.services;

import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.helpers.LogHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * <b>功能：</b>Debug Log<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/07 07:47 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
class DebugLog {

    private static final String PATH = "log";

    private String mPath;
    private String today = "";
    private FileWriter mWriter;
    private static final SimpleDateFormat TIMESTAMP_FMT = new SimpleDateFormat("[HH:mm:ss.SSS] ", Locale.getDefault());

    DebugLog() throws IOException {
        tryToDisableMediaScan();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void tryToDisableMediaScan() throws IOException {
        String path = Utils.getCachePath(PATH);
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
            // do not allow media scan
            new File(dir, ".nomedia").createNewFile();
        }
        openNewLogFile();
    }

    /**
     * 打开一个新的log文件以便记录
     */
    private void openNewLogFile() throws IOException {
        String string = getTodayString();
        if (Utils.isEmpty(today) || !today.equals(string)) {

            close();

            today = string;
            String path = Utils.getCachePath(PATH) + "/debug.log";
            File f = new File(path + "." + getTodayString() + ".txt");
            mPath = f.getAbsolutePath();
            mWriter = new FileWriter(f, true);

            LogHelper.log("DebugLog", "Opened log at: " + getPath());
        }
    }

    private static String getTodayString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return df.format(new Date());
    }

    String getPath() {
        return mPath;
    }

    void println(String message) throws IOException {

        openNewLogFile();

        mWriter.append(TIMESTAMP_FMT.format(new Date()));
        mWriter.append(message);
        mWriter.append("\r\n");
        mWriter.flush();
    }

    public void close() throws IOException {
        if (null != mWriter) {
            mWriter.append("=========== closed ===========\r\n\r\n");
            mWriter.close();
        }
    }
}
