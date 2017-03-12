package com.hlk.wbs.tx;

import com.hlk.wbs.tx.custom.CustomConvert;

/**
 * <b>功能</b>：TX数据通讯协议<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/05 13:35 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class TX extends Packable {

    /**
     * 终端类型
     */
    public static class TerminalType {
        /**
         * 10G终端
         */
        public static byte TX10G_APP = 0x31;
    }

    /**
     * 通讯协议版本。
     */
    public static byte ProtocolVersion = 0x10;

    /**
     * 通讯方式：TCP
     */
    public static class ProtocolType {
        public static final byte TCP = 0x00;
        public static final byte UDP = 0x10;
        public static final byte SMS = 0x20;
    }

    /**
     * 终端号码的长度。
     */
    public static byte TerminalIDLength = 0x06;

    private byte protocolVersion = ProtocolVersion;
    private short commandID;
    public short sequenceID = 0;
    private byte[] terminalID = new byte[TerminalIDLength];
    public byte packageId = 0x00, totalPackage = 0x00;

    /**
     * 创建 TX10G 通讯协议基本包
     */
    public TX() {
        super();
    }

    /**
     * 设置数据包的命令字.
     *
     * @param value 命令字
     */
    public void setCommandID(short value) {
        commandID = value;
    }

    /**
     * 设置数据包的流水号.
     */
    public void setSequenceID(short value) {
        sequenceID = value;
    }

    /**
     * 设置终端号码，如：13999999999，也即 phoneNo。
     */
    public void setTerminalID(String value) {
        terminalID = CustomConvert.hexStringToBytes(value);
    }
    /** 设置数据包的用户数据，不包含包头和包尾. */
    //public void setDataContent(byte[] value)
    //{
    //	data = new byte[value.length];
    //	System.arraycopy(value, 0, data, 0, value.length);
    //}

    /**
     * 重新打包 totalLength 字段。
     *
     * @param length 需要打包的总长度。
     */
    protected void packageTotalLength(short length) {
        totalLength = length;
        byte[] tmp = CustomConvert.shortToBytes(totalLength);
        packageData(tmp, 0);
    }

    /**
     * 将所有数据按照 TX10G 通讯协议进行打包
     */
    public void packageMessage() {
        iIndex = 0;
        content = null;
        packageData(CustomConvert.shortToBytes(totalLength));
        // 打包 protocolType, terminalType
        packageData(new byte[]{ProtocolType.UDP, TerminalType.TX10G_APP});
        // 打包 command_id
        packageData(CustomConvert.shortToBytes(commandID));
        // 打包 protocolVersion
        packageData(new byte[]{protocolVersion});
        // 打包 sequenceID
        packageData(CustomConvert.shortToBytes(sequenceID));
        // 打包 terminalID
        packageData(terminalID);
        // 打包 packageID, totalPackage
        packageData(new byte[]{packageId, totalPackage});
        // 重新打包 totalLength，在这里不需要打包校验和字节。
        packageTotalLength(iIndex);
    }
}
