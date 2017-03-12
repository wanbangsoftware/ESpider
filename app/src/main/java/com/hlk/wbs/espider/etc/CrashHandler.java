package com.hlk.wbs.espider.etc;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.hlk.wbs.espider.BuildConfig;
import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.helpers.LogHelper;
import com.hlk.wbs.espider.helpers.StringHelper;
import com.hlk.wbs.espider.tasks.AsyncExecutableTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.mail.MessagingException;

/**
 * UncaughtExceptionHandler：线程异常控制器是用来处理未捕获异常的。 如果程序出现了未捕获异常默认情况下则会出现强行关闭对话框
 * 实现该接口并注册为程序中的默认未捕获异常处理 这样当未捕获异常发生时，就可以做些异常处理操作 例如：收集异常信息，发送错误报告 等。
 * <p/>
 * UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告.
 */
public class CrashHandler implements UncaughtExceptionHandler {

    /**
     * Debug Log Tag
     */
    private static final String TAG = "CrashHandler";
    /**
     * CrashHandler实例
     */
    private static CrashHandler INSTANCE;
    /**
     * 程序的Context对象
     */
    private ContextWrapper mContext;
    /**
     * 系统默认的UncaughtException处理类
     */
    private UncaughtExceptionHandler mDefaultHandler;

    private static SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    /**
     * 使用Properties来保存设备的信息和错误堆栈信息
     */
    // private Properties mDeviceCrashInfo = new Properties();
    private static final String LINE_END = "\r\n";
    private static final String LINE = "*********************************";
    private static final String COLON = ": ";
    private static final String FMT = "%35s";
    /**
     * 内部版本号
     */
    private static final String INTERNAL_VERSION = StringHelper.getString(R.string.app_internal_version);
    /**
     * 错误报告文件的扩展名
     */
    private static final String FILE_EXTENSION = ".txt";
    /**
     * 错误报告文件夹
     */
    private static final String CRASH_DIR = "exception";
    private static final String LEFT_STUFF = "[";
    private static final String RIGHT_STUFF = "] ";
    /**
     * 错误堆栈信息
     */
    private StringBuilder crash = new StringBuilder();
    private StringBuilder debug = new StringBuilder();

    // 本地机器型号信息
    private String deviceInfo = "";
    private String manufacture = "";
    private String emailContent = "";

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        if (INSTANCE == null)
            INSTANCE = new CrashHandler();
        return INSTANCE;
    }

    /**
     * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器, 设置该CrashHandler为程序的默认处理器
     *
     * @param ctx ContextWrapper
     */
    public void init(ContextWrapper ctx) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        emailContent = readFileFromRaw(mContext, R.raw.email_content);
    }

    public synchronized void log(String tag, String string) {
        debug.append(LEFT_STUFF).append(dtf.format(new Date())).append(RIGHT_STUFF).append(tag).append(COLON).append(string).append(LINE_END);
    }

    public synchronized void clearDebugLog() {
        debug.setLength(0);
    }

    private String readFileFromRaw(Context context, int rawFileId) {
        StringBuilder sb = new StringBuilder();
        try {
            InputStream myFile = context.getResources().openRawResource(rawFileId);
            BufferedReader br = new BufferedReader(new InputStreamReader(myFile, "gb2312"));
            String tmp;
            while ((tmp = br.readLine()) != null) {
                sb.append(tmp);
            }
            br.close();
            myFile.close();
        } catch (IOException ee) {
            ee.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            // logcat.stop();
            // Sleep一会后结束程序
            // 来让线程停止一会是为了显示Toast信息给用户，然后Kill程序
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
     *
     * @param ex 异常
     * @return true:如果处理了该异常信息;否则返回false
     */
    private boolean handleException(final Throwable ex) {
        crash.setLength(0);
        if (ex == null) {
            return true;
        }
        // final String msg = ex.getLocalizedMessage();
        // 使用Toast来显示异常信息
//        new Thread() {
//            @Override
//            public void run() {
//                // Toast 显示需要出现在一个线程的消息队列中
//                Looper.prepare();
//                //Utility.showDialog(null, "程序运行期间发生致命错误需要关闭\n您要把出错的信息发送给开发者让其改进这些错误吗？");
//                Looper.loop();
//            }
//        }.start();
        try {
            new HandlerTask().exec(ex).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return true;
    }

    private class HandlerTask extends AsyncExecutableTask<Throwable, Void, Void> {

        @Override
        protected Void doInTask(Throwable... params) {
            Throwable ex = params[0];
            // 收集设备信息
            collectCrashDeviceInfo(mContext);
            // 保存错误报告文件
            // String crashFileName =
            saveCrashInfoToFile(ex);
            // 发送错误报告到服务器
            // sendCrashReportsToServer(mContext);

            return null;
        }

        @Override
        protected void doAfterExecute() {

        }
    }

    private String formatSize(double size) {
        return com.hlk.hlklib.etc.Utility.formatSize(size);
    }

    /**
     * 收集程序崩溃的设备信息
     *
     * @param ctx context
     */
    public void collectCrashDeviceInfo(Context ctx) {
        try {
            // Class for retrieving various kinds of information related to the
            // application packages that are currently installed on the device.
            // You can find this class through getPackageManager().
            PackageManager pm = ctx.getPackageManager();
            // getPackageInfo(String packageName, int flags)
            // Retrieve overall information about an application package that is
            // installed on the system.
            // public static final int GET_ACTIVITIES
            // Since: API Level 1 PackageInfo flag: return information about
            // activities in the package in activities.
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                // public String versionName The version name of this package,
                // as specified by the <manifest> tag's versionName attribute.
                crash.append(format(FMT, "Basic information")).append(COLON).append(LINE).append(LINE_END);
                crash.append(format(FMT, "ClientType")).append(COLON).append(String.valueOf(App.getInstance().client)).append(LINE_END);
                crash.append(format(FMT, "VersionName")).append(COLON).append(pi.versionName == null ? "not set" : pi.versionName).append(LINE_END);
                // public int versionCode The version number of this package,
                // as specified by the <manifest> tag's versionCode attribute.
                crash.append(format(FMT, "VersionCode")).append(COLON).append(pi.versionCode).append(LINE_END);
                crash.append(format(FMT, "InternalVersion")).append(COLON).append(INTERNAL_VERSION).append(LINE_END);
                crash.append(format(FMT, "DeviceId")).append(COLON).append(App.getInstance().getDeviceId()).append(LINE_END);
                crash.append(format(FMT, "ExceptionTime")).append(COLON).append(dtf.format(new Date())).append(LINE_END);

                // Build.VERSION
                crash.append(LINE_END).append(format(FMT, "Build version")).append(COLON).append(LINE).append(LINE_END);
                Field[] fields = Build.VERSION.class.getFields();
                for (Field field : fields) {
                    String name = field.getName();
                    if (name.contains("CODENAMES")) {
                        crash.append(format(FMT, name)).append(COLON).append(array((String[]) field.get(null))).append(LINE_END);
                    } else {
                        crash.append(format(FMT, name)).append(COLON).append(field.get(null)).append(LINE_END);
                    }
                }

                // 加入系统堆大小
                crash.append(LINE_END).append(format(FMT, "Memory")).append(COLON).append(LINE).append(LINE_END)
                        .append(format(FMT, "HEAP_MAXIMUM")).append(COLON).append(formatSize((Runtime.getRuntime().maxMemory()))).append(LINE_END)
                        .append(format(FMT, "HEAP_AVAILABLE")).append(COLON).append(formatSize(Runtime.getRuntime().freeMemory())).append(LINE_END)
                        .append(format(FMT, "HEAP_USED")).append(COLON).append(formatSize(Runtime.getRuntime().totalMemory())).append(LINE_END);

            }
        } catch (Exception e) {
            LogHelper.log(TAG, "Error while collect package info ", e);
        }
        crash.append(LINE_END).append(format(FMT, "Build information")).append(COLON).append(LINE).append(LINE_END);
        // 使用反射来收集设备信息.在Build类中包含各种设备信息,
        // 例如: 系统版本号,设备生产商 等帮助调试程序的有用信息
        // 返回 Field 对象的一个数组，这些对象反映此 Class 对象所表示的类或接口所声明的所有字段
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                // setAccessible(boolean flag)
                // 将此对象的 accessible 标志设置为指示的布尔值。
                // 通过设置Accessible属性为true,才能对私有变量进行访问，不然会得到一个IllegalAccessException的异常
                field.setAccessible(true);
                String name = field.getName();
                if (name.contains("_ABIS")) {
                    // 获取字符串数组的值
                    crash.append(format("%35s", name)).append(COLON).append(array((String[]) field.get(null))).append(LINE_END);
                } else {
                    crash.append(format("%35s", name)).append(COLON).append(field.get(null)).append(LINE_END);
                }
                if (name.toUpperCase(Locale.getDefault()).equals("MODEL")) {
                    deviceInfo = (String) field.get(null);
                }
                if (name.toUpperCase(Locale.getDefault()).equals("MANUFACTURER")) {
                    manufacture = (String) field.get(null);
                }
            } catch (Exception e) {
                LogHelper.log(TAG, "Error while collect crash info", e);
            }
        }
    }

    private String array(String[] array) {
        String ret = "";
        for (String string : array)
            ret += string + ", ";
        return ret;
    }

    private String format(String fmt, Object... args) {
        return String.format(fmt, args);
    }

    public static String getThrowableString(Throwable ex) {
        if (null == ex) return "null throwable object.";

        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);

        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        String result = info.toString();
        printWriter.close();
        return result;
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex exception
     */
    private void saveCrashInfoToFile(Throwable ex) {

        String result = getThrowableString(ex);

        if (BuildConfig.DEBUG) {
            // 调试阶段时显示 trace 信息
            LogHelper.log(TAG, result);
        }

        try {
            postReport(crash.toString(), result, debug.toString());
            // 保存为文件
            // crash.append(STACK_TRACE).append(" ***********************\r\n").append(result);
        } catch (Exception e) {
            log(TAG, format("An error occurred while post crash report: %s", e.getMessage()));
            log(TAG, format("Stack trace: %s", getThrowableString(e.getCause())));
            log(TAG, format("Cause: %s", result));
            try {
                saveCrashReportToFile();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        debug.setLength(0);
        crash.setLength(0);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveCrashReportToFile() throws IOException {
        long timestamp = System.currentTimeMillis();
        String fileName = "crash-" + String.valueOf(timestamp) + FILE_EXTENSION;

        String path = Utils.getCachePath(CRASH_DIR);
        File f = new File(path);
        if (!f.exists())
            f.mkdirs();

        path += fileName;
        // 保存文件
        FileOutputStream trace = new FileOutputStream(path);
        trace.write(crash.toString().getBytes());
        trace.write("\n".getBytes());
        trace.write(debug.toString().getBytes());
        trace.close();
    }

    /**
     * 把错误报告发送给服务器,包含新产生的和以前没发送的.
     */
    private void sendCrashReportsToServer() {
        try {
            String[] crFiles = getCrashReportFiles();
            if (crFiles != null && crFiles.length > 0) {
                postReport();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取错误报告文件名列表
     */
    private String[] getCrashReportFiles() {
        String path = Utils.getCachePath(CRASH_DIR);
        if (Utils.isEmpty(path)) return null;

        File filesDir = new File(path);
        // 实现FilenameFilter接口的类实例可用于过滤器文件名
        FilenameFilter filter = new FilenameFilter() {
            // 测试指定文件是否应该包含在某一文件列表中。
            public boolean accept(File dir, String name) {
                return name.endsWith(FILE_EXTENSION);
            }
        };
        // 返回一个字符串数组，这些字符串指定此抽象路径名表示的目录中满足指定过滤器的文件和目录
        return filesDir.list(filter);
    }

    // 报告当前的logcat
    public void report(Throwable ex) {
        try {
            new HandlerTask().exec(ex).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将收集到的信息发送邮件
     */
    private void postReport(String deviceParam, String stack_trace, String logcat) throws MessagingException {
        // 使用HTTP Post 发送错误报告到服务器
        // 这里不再详述,开发者可以根据OPhoneSDN上的其他网络操作
        // 教程来提交错误报告
        EmailSender sender = new EmailSender();
        // 设置服务器地址和端口
        sender.setProperties("www.wanbangsoftware.com", "25");
        // 添加邮件正文
        String content = emailContent
                // 机器参数列表
                .replace("%device_paramenters%", deviceParam)
                // 堆栈调用信息
                .replace("%stack_trace%", stack_trace)
                // 日志
                .replace("%logcat%", logcat);
        String version = App.getInstance().version();
        // 分别设置发件人，邮件标题和文本内容
        sender.setMessage("hsiang.leekwok@wanbangsoftware.com",
                StringHelper.format("eSpider(%s(%s), %s, %s)", version, INTERNAL_VERSION, manufacture, deviceInfo), content);
        // 设置收件人
        //if (BuildConfig.DEBUG) {
        sender.setReceiver(new String[]{"hsiang.leekwok@wanbangsoftware.com"});
        //} else {
        //    sender.setReceiver(new String[]{"hsiang.leekwok@wanbangsoftware.com"});
        //}
        // 发送邮件
        sender.sendEmail("www.wanbangsoftware.com", "hsiang.leekwok", "xlg_110004");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void postReport() throws MessagingException {
        // 使用HTTP Post 发送错误报告到服务器
        // 这里不再详述,开发者可以根据OPhoneSDN上的其他网络操作
        // 教程来提交错误报告
        EmailSender sender = new EmailSender();
        // 设置服务器地址和端口
        sender.setProperties("www.wanbangsoftware.com", "25");
        // 分别设置发件人，邮件标题和文本内容
        sender.setMessage("hsiang.leekwok@wanbangsoftware.com", "eSpider Attachment", "这是来自eSpider的异常信息");
        // 设置收件人
        sender.setReceiver(new String[]{"hsiang.leekwok@wanbangsoftware.com"});
        // 添加附件
        // sender.addAttachment(file);
        String path = Utils.getCachePath(CRASH_DIR);
        File f = new File(path);
        String[] files = null;
        // 将异常文件夹下的所有为发送的异常文件都发送出去
        if (f.isDirectory()) {
            files = f.list();
            for (String ff : files) {
                sender.addAttachment(path + ff);
            }
        }

        // 发送邮件
        sender.sendEmail("www.wanbangsoftware.com", "hsiang.leekwok", "xlg_110004");

        // 删除已经当作附件发送到服务器上的异常文件
        if (f.isDirectory()) {
            if (null != files) {
                for (String ff : files) {
                    File _file = new File(path + ff);
                    if (_file.exists())
                        _file.delete();
                }
            }
        }
    }

    /**
     * 在程序启动时候, 可以调用该函数来发送以前没有发送的报告
     */
    public void sendPreviousReportsToServer() {
        try {
            new SendPrevious().exec().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SendPrevious extends AsyncExecutableTask<Void, Void, Void> {
        @Override
        protected Void doInTask(Void... params) {
            sendCrashReportsToServer();
            return null;
        }

        @Override
        protected void doAfterExecute() {

        }
    }
}
