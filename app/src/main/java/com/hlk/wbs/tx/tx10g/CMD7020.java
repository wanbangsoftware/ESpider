package com.hlk.wbs.tx.tx10g;

import com.hlk.wbs.tx.Location;

/**
 * <b>功能</b>：TX10G的报警信息<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/05 14:18 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class CMD7020 extends TX10G {

    /**
     * 报警类别
     */
    public byte alarm;
    /**
     * 报警时的定位信息
     */
    public Location location;

    public CMD7020() {
        setCommandID(GPS_ALARM);
        location = new Location();
    }

    @Override
    public void packageMessage() {
        super.packageMessage();
        packageData(new byte[]{CSQ, alarm});
        location.alarm = true;
        location.packageMessage();
        packageData(location.getContent());
        // 重新打包 totalLength，这里加 1 是为了预留出来校验和字节
        packageTotalLength((short) (iIndex + 1));
        // 计算校验和
        packageCheckSum();
    }
}
