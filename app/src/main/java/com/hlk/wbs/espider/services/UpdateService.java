package com.hlk.wbs.espider.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.api.Api;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.callbacks.OnTaskExecuteListener;
import com.hlk.wbs.espider.etc.Action;
import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.helpers.StringHelper;
import com.hlk.wbs.espider.models.JsonResult;
import com.hlk.wbs.espider.models.Updater;
import com.hlk.wbs.espider.tasks.AsyncExecutableTask;
import com.hlk.wbs.espider.tasks.SimpleHttpTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

/**
 * <b>功能</b>：后台更新的service<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/04/04 08:59 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class UpdateService extends LocationService {

    private static final int ID = 0x00FF00FF;

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_INFO = "info";
    public static final String EXTRA_URL = "url";

    /**
     * 获取更新信息
     */
    protected void fetchingUpdate() {
        new SimpleHttpTask()
                .addOnTaskExecuteListener(mOnTaskExecuteListener)
                .exec(Api.ApiUrl(), Api.CheckUpdate(getUpdater()));
    }

    private OnTaskExecuteListener mOnTaskExecuteListener = new OnTaskExecuteListener() {
        @Override
        public void onPrepared() {

        }

        @Override
        public void doneInBackground(JsonResult result) {

        }

        @Override
        public void onComplete(JsonResult result) {
            if (null == result) {
                warningNoUpdate(ERR_DATA);
            } else {
                switch (result.State) {
                    case 0:
                        if (!Utils.isEmpty(result.Data)) {
                            warningUpdatable(result.Data);
                        } else {
                            warningNoUpdate(ERR_DATA);
                        }
                        break;
                    case -2:
                        warningNoUpdate(NO_UPDATE);
                        break;
                    default:
                        warningNoUpdate(ERR_UPDATE);
                        break;
                }
            }
            //warningUpdatable("1.0.1", "一些常用的更新", "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk");
        }
    };

    private Updater getUpdater() {
        Updater updater = new Updater();
        long intervalVersion = Long.valueOf(StringHelper.getString(R.string.app_internal_version));
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            updater.InternalVersion = intervalVersion;
            updater.VersionCode = info.versionCode;
            updater.VersionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return updater;
    }

    private void warningUpdatable(String json) {
        Updater updater;
        try {
            updater = App.getInstance().Gson().fromJson(json, Updater.class);
        } catch (Exception ignore) {
            updater = null;
            warningNoUpdate(ERR_DATA);
        }
        if (null != updater) {
            try {
                long intervalVersion = Long.valueOf(StringHelper.getString(R.string.app_internal_version));
                PackageManager manager = getPackageManager();
                PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
                if (updater.VersionCode > info.versionCode ||
                        updater.VersionName.compareTo(info.versionName) > 0 ||
                        updater.InternalVersion > intervalVersion) {
                    String language = Locale.getDefault().getLanguage();
                    String text = language.equals("zh") ? updater.Description_zh : updater.Description_en;
                    warningUpdatable(updater.VersionName, text, updater.Download);
                } else {
                    warningNoUpdate(NO_UPDATE);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                warningNoUpdate(ERR_HANDLER);
            }
        }
    }

    /**
     * 无可用更新
     */
    public static final int NO_UPDATE = 0;
    /**
     * 服务器无法处理请求
     */
    public static final int ERR_UPDATE = -1;
    /**
     * 本地处理失败
     */
    public static final int ERR_HANDLER = -2;
    /**
     * 服务器返回的数据错误
     */
    public static final int ERR_DATA = -3;

    /**
     * 通知前台没有可用的更新
     */
    private void warningNoUpdate(int reason) {
        Intent intent = new Intent(Action.UPDATE_FAIL_ACTION);
        intent.putExtra(EXTRA_TITLE, reason);
        sendBroadcast(intent);
    }

    /**
     * 通知app前台有可用的更新
     */
    private void warningUpdatable(String version, String info, String url) {
        String title = StringHelper.getString(R.string.ui_warning_updating_title, version);
        //format("有新的版本了(%s)", v1);
        info = info.replaceAll("\n", "<br />");
        Intent intent = new Intent(Action.UPDATABLE_ACTION);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_INFO, info);
        intent.putExtra(EXTRA_URL, url);
        sendBroadcast(intent);
    }

    protected void update(String url) {
//        //http = "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk";
        new Update().exec(url);
    }

    private class Update extends AsyncExecutableTask<String, Void, Void> {

        NotificationManager manager;
        NotificationCompat.Builder builder;
        boolean downloaded = false;
        private String path, url;

        @Override
        protected void doBeforeExecute() {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            builder = new NotificationCompat.Builder(UpdateService.this);
            builder.setContentTitle(StringHelper.getString(R.string.ui_notification_updating_title))
                    .setContentText(StringHelper.getString(R.string.ui_notification_updating_description))
                    .setContentIntent(null)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setSmallIcon(R.mipmap.img_app_icon);
        }

        /**
         * 清理之前存在的apk文件
         */
        @SuppressWarnings("ResultOfMethodCallIgnored")
        private void clearApks() {
            String path = Utils.getCachePath(Utils.OTHER_DIR);
            File dir = new File(path);
            if (dir.isDirectory()) {
                File[] files = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".apk");
                    }
                });
                for (File file : files) {
                    log(format("delete file: %s", file.getAbsolutePath()));
                    file.delete();
                }
            }
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        protected Void doInTask(String... params) {
            try {
                // 清理空间
                clearApks();
                this.url = params[0];
                path = format("%s%s.apk", Utils.getCachePath(Utils.OTHER_DIR), Utils.format("yyyyMMddHHmmss", new Date()));

                URL url = new URL(this.url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept-Encoding", "identity");
                conn.setConnectTimeout(5000);
                conn.connect();
                if (conn.getResponseCode() == 200) {
                    int length = conn.getContentLength();
                    File f = new File(path);
                    if (f.exists()) {
                        f.delete();
                    }
                    InputStream inStream = conn.getInputStream();
                    FileOutputStream fos = new FileOutputStream(f);
                    byte[] buf = new byte[1024];
                    int ch, count = 0, lst = 0, progress;
                    while ((ch = inStream.read(buf)) != -1) {
                        fos.write(buf, 0, ch);
                        count += ch;
                        progress = (int) ((count * 1.0 / length) * 100);
                        if (lst != progress) {
                            lst = progress;
                            builder.setProgress(length, count, false);
                            manager.notify(ID, builder.build());
                        }
                    }
                    downloaded = true;
                    fos.flush();
                    fos.close();
                    inStream.close();
                    notifyDownloadComplete();
                }
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void doAfterExecute() {
            if (downloaded) {
                startActivity(getViewIntent());
            } else {
                //ToastHelper.showMsg("无法下载更新资源");
                notifyDownloadFail();
            }
        }

        private Intent getViewIntent() {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
            return intent;
        }

        private void notifyDownloadComplete() {
            PendingIntent pendingIntent = PendingIntent.getActivity(UpdateService.this, 0, getViewIntent(), 0);
            builder.setContentText(StringHelper.getString(R.string.ui_notification_updated_description))
                    .setContentTitle(StringHelper.getString(R.string.ui_notification_updated_title))
                    .setOngoing(false)
                    .setProgress(0, 0, false).setContentIntent(pendingIntent);
            manager.notify(ID, builder.build());
        }

        private void notifyDownloadFail() {
            Intent intent = new Intent(Action.UPDATE_ACTION);
            intent.putExtra(EXTRA_URL, url);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(UpdateService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentText(StringHelper.getString(R.string.ui_notification_update_fail_description))
                    .setContentTitle(StringHelper.getString(R.string.ui_notification_update_fail_title))
                    .setContentIntent(pendingIntent)
                    .setProgress(0, 0, false).setOngoing(false);
            manager.notify(ID, builder.build());
        }
    }
}
