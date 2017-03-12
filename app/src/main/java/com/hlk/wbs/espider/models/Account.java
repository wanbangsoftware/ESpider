package com.hlk.wbs.espider.models;

/**
 * <b>功能：</b><br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/08 08:22 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class Account {
    /**
     * 账户
     */
    public String name;
    /**
     * 已加密的密码
     */
    public String md5;
    /**
     * 设备号
     */
    public String device;
    /**
     * 登录的session id
     */
    public String session;
    /**
     * 其他数据
     */
    public String data;

    public static final int NONE = -1;
    /**
     * 需要绑定账户
     */
    public static final int BIND = 0;
    /**
     * 需要重新绑定账号
     */
    public static final int REBIND = 1;

    /**
     * 用户所处区域
     */
    public static class Belongs {
        /**
         * 蒙古
         */
        public static final String MNG = "MNG";
        /**
         * 中国
         */
        public static final String CHN = "CHN";
    }
}
