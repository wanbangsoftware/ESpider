package com.hlk.wbs.espider.helpers;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.hlk.wbs.espider.applications.App;

import java.util.List;

/**
 * 百度地图相关功能Helper<br />
 * 作者：Hsiang Leekwok on 2015/08/29 09:08<br />
 * 邮箱：xiang.l.g@gmail.com<br />
 */
public class BaiduApiHelper {

    /**
     * 百度地图api名字
     */
    public static final String NAME = "baidu";

    private static BaiduApiHelper instance;

    // 默认定位次数为10次后停止定位
    private static final int LOCATE_TIMER = 10;

    private LocationClient mLocationClient = null;

    /**
     * 获取一个全局实例
     */
    public static BaiduApiHelper Instance() {
        if (null == instance) {
            instance = new BaiduApiHelper();
        }
        return instance;
    }

    BaiduApiHelper() {
        //声明LocationClient类
        mLocationClient = new LocationClient(App.getInstance());
        initLocation();
        //注册监听函数
        BDLocationListener myListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myListener);
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        /**
         * 可选，默认gcj02，设置返回的定位结果坐标系
         * 定位SDK可以返回bd09、bd09ll、gcj02三种类型坐标，
         * 若需要将定位点的位置通过百度Android地图 SDK进行地图展示，
         * 请返回bd09ll，将无偏差的叠加在百度地图上。
         * */
        option.setCoorType("bd09ll");

        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan((int) scanSpan);

        // 可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);

        // 可选，默认false,设置是否使用gps
        option.setOpenGps(true);

        // 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        //option.setLocationNotify(false);

        // 可选，默认false，设置是否需要位置语义化结果，
        // 可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(true);

        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        //option.setIsNeedLocationPoiList(true);

        //可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.setIgnoreKillProcess(false);

        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.SetIgnoreCacheException(false);

        //可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setEnableSimulateGps(false);

        // 返回的定位结果包含手机机头的方向
        option.setNeedDeviceDirect(true);
        mLocationClient.setLocOption(option);
    }

    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location

            if (showLocation) {
                StringBuilder sb = new StringBuilder();
                sb.append("time : ");
                sb.append(location.getTime());
                sb.append("\nerror code : ");
                sb.append(location.getLocType());
                sb.append("\nlatitude : ");
                sb.append(location.getLatitude());
                sb.append("\nlontitude : ");
                sb.append(location.getLongitude());
                sb.append("\nradius : ");
                sb.append(location.getRadius());
                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(location.getSpeed());// 单位：公里每小时
                    sb.append("\nsatellite : ");
                    sb.append(location.getSatelliteNumber());
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 单位：米
                    sb.append("\ndirection : ");
                    sb.append(location.getDirection());// 单位度
                    sb.append("\naddr : ");
                    sb.append(location.getAddrStr());
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");

                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    sb.append("\naddr : ");
                    sb.append(location.getAddrStr());
                    //运营商信息
                    sb.append("\noperationers : ");
                    sb.append(location.getOperators());
                    sb.append("\ndescribe : ");
                    sb.append("网络定位成功");
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
                sb.append("\nlocationdescribe : ");
                sb.append(location.getLocationDescribe());// 位置语义化信息
                List<Poi> list = location.getPoiList();// POI数据
                if (list != null) {
                    sb.append("\npoilist size = : ");
                    sb.append(list.size());
                    for (Poi p : list) {
                        sb.append("\npoi= : ");
                        sb.append(p.getId()).append(" ").append(p.getName()).append(" ").append(p.getRank());
                    }
                }
                LogHelper.log("BaiduLocationApiDemo", sb.toString());
            }

            boolean located = located(location.getLocType());

            // 返回回调
            if (null != mOnLocatedListener) {
                mOnLocatedListener.onLocated(located, location);
            }

            // 获取到定位之后停止定位
            if (locateTime <= 0 || (located && stopWhenLocated)) {
                stop();
            } else {
                locateTime--;
            }
        }

        private boolean located(int locationType) {
            return locationType == BDLocation.TypeGpsLocation ||
                    locationType == BDLocation.TypeNetWorkLocation ||
                    locationType == BDLocation.TypeOffLineLocation;
        }
    }

    private int locateTime;

    /**
     * 开始定位
     */
    public void start() {
        locateTime = LOCATE_TIMER;
        mLocationClient.start();
    }

    /**
     * 停止定位
     */
    public void stop() {
        mLocationClient.stop();
        // 停止之后释放listener
        mOnLocatedListener = null;
    }

    private boolean showLocation = false;

    /**
     * 设置是否在定位成功后打印debug信息
     */
    public BaiduApiHelper showDebug(boolean shown) {
        showLocation = shown;
        return this;
    }

    private long scanSpan = 2000;

    /**
     * 设置定位时间间隔，单位毫秒，置为0时表示只定位一次
     */
    public BaiduApiHelper setScanInterval(long interval) {
        this.scanSpan = interval;
        initLocation();
        return this;
    }

    private boolean stopWhenLocated = true;

    /**
     * 设置是否在定位成功之后自动停止获取定位
     */
    public BaiduApiHelper stopWhenLocated(boolean stopWhenLocated) {
        this.stopWhenLocated = stopWhenLocated;
        return this;
    }

    private OnBDLocatedListener mOnLocatedListener;

    /**
     * 设置定位成功之后的回调处理
     */
    public BaiduApiHelper addOnLocatedListener(OnBDLocatedListener l) {
        mOnLocatedListener = l;
        return this;
    }

    /**
     * <b>功能</b>：百度定位的回调<br />
     * <b>作者</b>：Hsiang Leekwok <br />
     * <b>时间</b>：2016/06/05 21:16 <br />
     * <b>邮箱</b>：xiang.l.g@gmail.com <br />
     */
    public interface OnBDLocatedListener {
        /**
         * 定位成功的回调处理过程
         *
         * @param success  定位是否成功
         * @param location 定位的详细信息
         */
        void onLocated(boolean success, BDLocation location);
    }
}
