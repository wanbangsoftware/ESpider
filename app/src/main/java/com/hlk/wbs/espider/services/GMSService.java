package com.hlk.wbs.espider.services;

/**
 * <b>功能</b>：采用Google Play Service进行定位的后台服务<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/01 01:47 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public abstract class GMSService extends BDService {

    protected void tryInitializeGpsService() {
    }

    /**
     * 初始化Google位置服务
     */
    protected void startGooglePlayLocationService() {
    }

    protected void stopGooglePlayLocationService() {
    }
}
