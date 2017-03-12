package com.hlk.wbs.tx;

import com.hlk.wbs.espider.BuildConfig;
import com.hlk.wbs.espider.helpers.LogHelper;
import com.hlk.wbs.tx.custom.CustomConvert;

import java.util.Locale;

/**
 * <b>功能</b>：能进行基本打包操作的类<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/05 13:57 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class Packable {

    /**
     * 当前数据包的总长度
     */
    protected short totalLength = 0;
    /**
     * The indexer which used in package message to package.
     */
    protected short iIndex = 0;
    /**
     * 数据包的内容
     */
    protected byte[] content;

    /**
     * 获取整个数据包的内容，包括挣个包头和包尾的全部数据.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * 将一个 byte 数组打包
     */
    protected void packageData(byte[] data) {
        int len = data.length;
        iIndex += len;
        packageData(data, iIndex - len);
    }

    /**
     * 在指定位置打包数据
     */
    protected void packageData(byte[] data, int start) {
        content = CustomConvert.expandArray(content, iIndex);
        System.arraycopy(data, 0, content, start, data.length);
    }

    /**
     * 计算校验和并打包到包尾部。
     */
    protected void packageCheckSum() {
        byte sum = CustomConvert.GetCheckSum(content);
        packageData(new byte[]{sum});
    }

    /**
     * 重新打包 totalLength 字段。
     *
     * @param data 需要打包的总长度。
     */
    protected void packageShort(short data) {
        byte[] tmp = CustomConvert.shortToBytes(data);
        packageData(tmp);
    }

    /**
     * 格式化字符串
     */
    protected String format(String format, Object... args) {
        return String.format(Locale.getDefault(), format, args);
    }

    protected void log(String string) {
        if (BuildConfig.DEBUG) {
            LogHelper.log(this.getClass().getSimpleName(), string);
        }
    }
}
