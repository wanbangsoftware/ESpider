package com.hlk.wbs.tx.tx10g;

import com.hlk.wbs.tx.TX;

/**
 * <b>功能</b>：TX10G主类<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/05 13:31 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class TX10G extends TX {

    /**
     * 报警信息
     */
    public static final short GPS_ALARM = 0x7020;
    /**
     * GPS汇报的命令字
     */
    public static final short GPS_REPORT = 0x7030;

    /**
     * 默认的CSQ大小
     */
    public static final byte CSQ = 0x1F;
}
