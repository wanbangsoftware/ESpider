package com.hlk.wbs.espider.tasks;

import android.Manifest;
import android.content.Intent;
import android.location.LocationManager;

import com.hlk.wbs.espider.api.Api;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.callbacks.OnTaskExecuteListener;
import com.hlk.wbs.espider.etc.Permission;
import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.helpers.StringHelper;
import com.hlk.wbs.espider.models.Account;
import com.hlk.wbs.espider.models.JsonResult;
import com.hlk.wbs.espider.models.Position;
import com.hlk.wbs.tx.Location;
import com.hlk.wbs.tx.custom.CustomConvert;
import com.hlk.wbs.tx.tx10g.Alarm;
import com.hlk.wbs.tx.tx10g.CMD7020;
import com.hlk.wbs.tx.tx10g.CMD7030;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.model.ConflictAlgorithm;

import java.util.Date;
import java.util.List;

/**
 * <b>功能：</b>汇报定位信息到服务器的线程<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2016/06/07 12:54 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class PeriodReportTask extends AsyncExecutableTask<Void, Void, JsonResult> {

    /**
     * 最大尝试发送的次数
     */
    private static final int MAX_RETRY_TIMES = 5;
    /**
     * 默认汇报时间间隔，单位分钟
     */
    public static final int DEFAULT_PERIOD = 10;
    private int retryTimes = 0;

    @Override
    protected void doBeforeExecute() {
        super.doBeforeExecute();
    }

    @Override
    protected JsonResult doInTask(Void... params) {
        // 网络不可用时直接返回不再继续
        if (!App.getInstance().isNetworkAvailable())
            return null;

        // 没有绑定账号时不发送任何数据
        if (Utils.isEmpty(App.getInstance().getLastAccount())) return null;

        List<Position> temp = query(false);
        // 将未上报完成的数据都上报上去
        while (null != temp && temp.size() > 0) {
            // 如果大于本次发送失败的尝试次数则退出，等待下一次再次发送
            if (retryTimes >= MAX_RETRY_TIMES) {
                log(StringHelper.format("Server cannot handle request %d times, wait for next time to try again...", retryTimes));
                break;
            }

            reportLocation(temp);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            temp = query(false);
        }
        if (retryTimes <= 0) {
            // 网络没问题时才继续上报报警信息
            reportAlarm();
        }
        return null;
    }

    private List<Position> query(boolean alarm) {
        // 查找未发送过的数据
        QueryBuilder<Position> builder = new QueryBuilder<>(Position.class).whereEquals(Position.Columns.Report, false);
        if (alarm) {
            builder.whereAppendAnd().whereNoEquals(Position.Columns.Alarm, Alarm.NoAlarm);
        } else {
            builder.whereAppendAnd().whereEquals(Position.Columns.Alarm, Alarm.NoAlarm);
        }
        builder.orderBy(Position.Columns.GpsTime).limit(0, CMD7030.MAX_LOCATION);
        try {
            if (Permission.hasPermission(App.getInstance(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                return App.Orm.query(builder);
            }
            return null;
        } catch (Exception e) {
            log("Cannot query from database, please check the permission.");
            return null;
        }
    }

    private void reportAlarm() {
        List<Position> positions = query(true);
        String terminal = App.getInstance().getLastAccount();
        if (null != positions && positions.size() > 0) {
            for (Position position : positions) {
                CMD7020 cmd7020 = new CMD7020();
                cmd7020.setTerminalID(terminal + "000");
                cmd7020.alarm = position.alarm;
                cmd7020.location.time = new Date(position.gpsTime);
                cmd7020.location.longitude = position.longitude;
                cmd7020.location.latitude = position.latitude;
                cmd7020.packageId = !StringHelper.isEmpty(position.provider) && position.provider.equals(LocationManager.GPS_PROVIDER) ? 0 : (byte) 0x80;
                cmd7020.packageMessage();

                String base64 = CustomConvert.base64(cmd7020.getContent());
                log(CustomConvert.bytesToHexString(cmd7020.getContent()));

                Account post = new Account();
                post.name = terminal;
                post.data = CustomConvert.urlEncode(base64);
                result = null;
                fetchingJson(Api.ApiUrl(), Api.Report(post));
                if (null != result && result.State == 0) {
                    position.report = true;
                    position.reportTime = new Date().getTime();
                    App.Orm.update(position, ConflictAlgorithm.Rollback);
                } else {
                    log(StringHelper.format("Server cannot handle this alarm report, wait for next time try again."));
                    break;
                }
            }
            notifyDataChanged();
        }
    }

    /**
     * 汇报数据并返回汇报的条数
     */
    private int reportLocation(List<Position> positions) {
        if (null != positions && positions.size() > 0) {
            // 如果本地缓存中有还未发送的数据则发送
            CMD7030 cmd7030 = new CMD7030();
            cmd7030.setTerminalID(App.getInstance().getLastAccount() + "000");
            // 0=gps,1=network
            char[] providers = new char[]{'0', '0', '0', '0', '0', '0', '0', '0',
                    '0', '0', '0', '0', '0', '0', '0', '0'};
            int i = 0;
            for (Position position : positions) {
                Location location = new Location();
                location.alarm = false;
                location.latitude = position.latitude;
                location.longitude = position.longitude;
                location.time = new Date(position.gpsTime);
                cmd7030.locations.add(location);
                if (!StringHelper.isEmpty(position.provider) && position.provider.equals(LocationManager.NETWORK_PROVIDER)) {
                    providers[i] = '1';
                }
                i++;
            }
            String string = new String(providers);
            String s1 = string.substring(0, 8);
            cmd7030.packageId = (byte) CustomConvert.DigitalToInt(s1, CustomConvert.Binary);
            s1 = string.substring(8);
            cmd7030.totalPackage = (byte) CustomConvert.DigitalToInt(s1, CustomConvert.Binary);
            cmd7030.packageMessage();
            log(CustomConvert.bytesToHexString(cmd7030.getContent()));
            Account account = new Account();
            account.name = App.getInstance().getLastAccount();
            String base64 = CustomConvert.base64(cmd7030.getContent());
            account.data = CustomConvert.urlEncode(base64);
            result = null;
            fetchingJson(Api.ApiUrl(), Api.Report(account));
            if (null != result && result.State == 0) {
                // 服务器接收成功
                for (Position position : positions) {
                    position.report = true;
                    position.reportTime = new Date().getTime();
                }
                // 更新本地缓存中的汇报标记，失败时Rollback当前所有更改
                App.Orm.update(positions, ConflictAlgorithm.Rollback);
                notifyDataChanged();
                return positions.size();
            } else {
                retryTimes++;
                log(StringHelper.format("Server cannot handle this posted data, has been retry %d times...", retryTimes));
            }
        }
        return 0;
    }

    /**
     * 通知前台UI数据已经发送到服务器
     */
    private void notifyDataChanged() {
        Intent intent = new Intent(OrmTask.ORM_DATA_CHANGED_ACTION);
        intent.putExtra(OrmTask.EFFECTED, Position.class.getName());
        App.getInstance().sendBroadcast(intent);
    }

    @Override
    protected void doAfterExecute() {
        if (null != mOnTaskExecuteListener) {
            mOnTaskExecuteListener.onComplete(result);
        }
    }

    @Override
    public PeriodReportTask addOnTaskExecuteListener(OnTaskExecuteListener l) {
        super.addOnTaskExecuteListener(l);
        return this;
    }

}
