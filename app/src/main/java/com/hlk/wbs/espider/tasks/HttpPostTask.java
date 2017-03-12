package com.hlk.wbs.espider.tasks;

import com.hlk.hlklib.tasks.AsyncedTask;
import com.hlk.wbs.espider.cert.TaskX509TrustManager;
import com.hlk.wbs.espider.helpers.LogHelper;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

/**
 * HTTP获取远程Json的Task，包括上传文件和Post数据
 * 作者：Hsiang Leekwok on 2015/08/25 21:54
 * 邮箱：xiang.l.g@gmail.com
 */
public abstract class HttpPostTask<Params, Progress, Result> extends AsyncedTask<Params, Progress, Result> {

    protected boolean loggable = true;

    @Override
    protected void log(String string) {
        if (loggable) {
            LogHelper.log(this.getClass().getSimpleName(), string, true);
        }
    }

    protected String postHttp(String... params) {
        // 如果Url中包含Upload字样则是上传文件
        if (params[0].indexOf("/Upload") > 10) {
            return postFiles(params);
        } else {
            return post(params);
        }
    }

    private String BOUNDARY = "---------7d4a6d158c9";

    /**
     * 通过post上传文件
     */
    private String postFiles(String... params) {
        String result = "";
        String baseUrl = params[0];
        log("url: " + baseUrl + ", \npost type: file");
        try {
            HttpURLConnection urlConnection = getConnection(baseUrl);
            urlConnection.setReadTimeout(30000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());

            // 加入文件
            int i = 1;
            while (i < params.length) {
                // 不为空则上传
                if (null != params[i] && params[i].length() > 10) {
                    postFile(String.format(Locale.getDefault(), "file%d", i), params[i], out);
                }
                i++;
            }
            out.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes());
            out.flush();
            out.close();

            // 读取返回数据
            result = getContent(urlConnection.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        log("result: " + result);
        return result;
    }

    private void postFile(String name, String path, DataOutputStream out) throws IOException {
        File file = new File(path);
        //log(String.format("try post file(size: %d): %s", (int) file.length(), path));
        String filename = file.getName();
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n").append("--").append(BOUNDARY).append("\r\n").
                append("Content-Disposition: form-data; name=\"").append(name).
                append("\"; filename=\"").append(filename).append("\"\r\n").
                append("Content-Type:application/octet-stream\r\n\r\n");
        byte[] data = sb.toString().getBytes();
        out.write(data);

        DataInputStream in = new DataInputStream(new FileInputStream(file));
        int bytes;
        byte[] bufferOut = new byte[1024];
        while ((bytes = in.read(bufferOut)) != -1) {
            out.write(bufferOut, 0, bytes);
        }
        //out.write("\r\n".getBytes()); //多个文件时，二个文件之间加入这个
        in.close();
    }

    private HttpURLConnection getConnection(String httpUrl) throws IOException {
        URL url = new URL(httpUrl);
        HttpURLConnection urlConnection;
        if (url.getProtocol().toLowerCase().equals("https")) {
            trustAllHosts();
            urlConnection = (HttpURLConnection) url.openConnection();
            // 不进行主机名确认
            ((HttpsURLConnection) urlConnection).setHostnameVerifier(DO_NOT_VERIFY);
        } else {
            urlConnection = (HttpURLConnection) url.openConnection();
        }
        urlConnection.setConnectTimeout(10000);
        urlConnection.setRequestMethod("POST");
        // 设置是否向connection输出，因为这个是post请求，参数要放在
        // http正文内，因此需要设为true
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setUseCaches(false);
        return urlConnection;
    }

    /**
     * 普通的json post
     */
    private String post(String... params) {
        String result = "";
        String baseUrl = params[0];
        String data = params[1];
        log("url: " + baseUrl + ", \ndata: " + data + ", \npost type: text");
        try {
            HttpURLConnection urlConnection = getConnection(baseUrl);
            urlConnection.setInstanceFollowRedirects(true);
            // 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
            // 意思是正文是urlencoded编码过的form参数，下面我们可以看到我们对正文内容使用URLEncoder.encode
            // 进行编码
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
            // 要注意的是connection.getOutputStream会隐含的进行connect。
            urlConnection.connect();
            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());

            //String content = data;
            out.write(data.getBytes("UTF-8"));
            out.flush();
            out.close();
            int resp = urlConnection.getResponseCode();
            if (resp == 200) {
                result = getContent(urlConnection.getInputStream());
            }
            urlConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log("result: " + result);
        return result;
    }

    /**
     * 获取服务器返回的json字符串
     */
    private String getContent(InputStream inputStream) {
        String result = "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int d = inputStream.read();
            while (d != -1) {
                baos.write(d);
                d = inputStream.read();
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 进度更新
     */
    public interface OnProgressChange {
        /**
         * 进度更新
         */
        void onProgress(int progress);
    }

    static TrustManager[] tmArray = new TaskX509TrustManager[]{new TaskX509TrustManager()};

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        // Android 采用X509的证书信息机制
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, tmArray, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            // HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY);
            // 不进行主机名确认
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            // System.out.println("Warning: URL Host: " + hostname + " vs. " + session.getPeerHost());
            return true;
        }
    };
}
