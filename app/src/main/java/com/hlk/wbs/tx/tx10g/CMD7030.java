package com.hlk.wbs.tx.tx10g;

import com.hlk.wbs.tx.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>功能</b>：TX10G的定位信息汇报<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/05 13:42 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class CMD7030 extends TX10G {

    /**
     * 最多可以一次发送的定位信息数量
     */
    public static final int MAX_LOCATION = 10;
    /**
     * 定位信息列表
     */
    public List<Location> locations = new ArrayList<>();

    public CMD7030() {
        setCommandID(GPS_REPORT);
    }

    /**
     * 将所有数据按照 TX10G 通讯协议打包。
     */
    @Override
    public void packageMessage() {
        super.packageMessage();
        if (locations.size() > MAX_LOCATION)
            throw new IllegalArgumentException("You can only send 10 location(s) in one times.");

        // 打包 csq
        packageData(new byte[]{CSQ, (byte) locations.size()});
        // 打包定位信息
        for (Location location : locations) {
            location.packageMessage();
            packageData(location.getContent());
        }
        // 重新打包 totalLength，这里加 1 是为了预留出来校验和字节
        packageTotalLength((short) (iIndex + 1));
        // 计算校验和
        packageCheckSum();
    }
}
