package com.hlk.wbs.tx.datetime;

/**
 * 创建 WbsDateTime 类时指定的字节数组长度不够或指定的字节数组是 null.
 */
public class WbsDateTimeNullException extends Exception {

    private static final long serialVersionUID = -8314567888456828404L;

    public String toString() {
        return "Can not use \"null byte[]\" or \"not enough length byte[]\" to convert WbsDateTime.";
    }
}