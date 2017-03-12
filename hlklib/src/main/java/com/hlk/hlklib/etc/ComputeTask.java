package com.hlk.hlklib.etc;

import com.hlk.hlklib.tasks.AsyncedTask;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * 获取指定文件的特征码，使用 new ComputTask().execute(filePath,"SHA-1").get()
 */
public class ComputeTask extends AsyncedTask<String, Void, String> {

    private String compute(String... params) {

        String result = null;

        InputStream inputStream = null;
        try {
            // Create an FileInputStream instance according to the filepath
            inputStream = new FileInputStream(params[0]);
            // The buffer to read the file
            byte[] buffer = new byte[2048];
            // Get a MD5 instance
            MessageDigest digest = MessageDigest.getInstance(params[1]);
            // Record how many bytes have been read
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0) {
                    // Update the digest
                    digest.update(buffer, 0, numRead);
                }
            }
            // Complete the hash computing
            // byte[] md5Bytes = digest.digest();
            // Call the function to convert to hex digits
            result = Cryptography.convertBytesToString(digest.digest());
        } catch (Exception ignored) {
            ignored.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    // Close the InputStream
                    inputStream.close();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    protected String doInBackground(String... params) {
        return compute(params);
    }
}
