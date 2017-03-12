package com.hlk.hlklib.etc;

import java.security.MessageDigest;

/**
 * 提供基本的加密/解密方法集合
 */
public class Cryptography {

    public static class Provider {
        public static final String MD5 = "MD5";
        public static final String SHA1 = "SHA-1";
        public static final String SHA256 = "SHA-256";
        public static final String SHA384 = "SHA-384";
        public static final String SHA512 = "SHA-512";
    }

    /**
     * 获取字节数组的16进制字符串，小写
     */
    public static String convertBytesToString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() < 2) {
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * Get the md5 value of the filepath specified file
     *
     * @param filePath The filepath of the file
     * @return The md5 value
     */
    public static String getFileMD5(String filePath) {
        return fileMessageDigest(filePath, Provider.MD5);
    }

    /**
     * Get the sha1 value of the filepath specified file
     *
     * @param filePath The filepath of the file
     * @return The sha1 value
     */
    public static String getFileSHA1(String filePath) {
        return fileMessageDigest(filePath, Provider.SHA1);
    }

    /**
     * Get the sha256 value of the filepath specified file
     *
     * @param filePath The filepath of the file
     * @return The sha256 value
     */
    public static String getFileSHA256(String filePath) {
        return fileMessageDigest(filePath, Provider.SHA256);
    }

    /**
     * Get the sha384 value of the filepath specified file
     *
     * @param filePath The filepath of the file
     * @return The sha384 value
     */
    public static String getFileSHA384(String filePath) {
        return fileMessageDigest(filePath, Provider.SHA384);
    }

    /**
     * Get the sha512 value of the filepath specified file
     *
     * @param filePath The filepath of the file
     * @return The sha512 value
     */
    public static String getFileSHA512(String filePath) {
        return fileMessageDigest(filePath, Provider.SHA512);
    }

    /**
     * 获取文件的指定信息
     *
     * @param filePath  文件路径
     * @param algorithm 算法
     * @return 字符串
     */
    private static String fileMessageDigest(String filePath, String algorithm) {
        String result = null;
        try {
            result = new ComputeTask().exec(filePath, algorithm).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 获取字符串的 MD5 码
     */
    public static String md5(String source) {
        return stringMessageDigest(source, Provider.MD5);
    }

    /**
     * 获取字符串的 SHA1 码
     */
    public static String sha1(String source) {
        return stringMessageDigest(source, Provider.SHA1);
    }

    /**
     * 获取字符串的 SHA256 码
     */
    public static String sha256(String source) {
        return stringMessageDigest(source, Provider.SHA256);
    }

    /**
     * 获取字符串的 SHA384 码
     */
    public static String sha384(String source) {
        return stringMessageDigest(source, Provider.SHA384);
    }

    /**
     * 获取字符串的 SHA512 码
     */
    public static String sha512(String source) {
        return stringMessageDigest(source, Provider.SHA512);
    }

    /**
     * Get the sha1 value of a string
     *
     * @param source string
     * @return the sha value
     */
    private static String stringMessageDigest(String source, String algorithm) {
        return getMessageDigest(source.getBytes(), algorithm);
    }

    private static String getMessageDigest(byte[] data, String algorithm) {
        try {
            MessageDigest digester = MessageDigest.getInstance(algorithm);
            digester.update(data);
            return convertBytesToString(digester.digest());
        } catch (Exception ignore) {
            return null;
        }
    }
}
