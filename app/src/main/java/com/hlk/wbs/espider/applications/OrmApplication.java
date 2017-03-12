package com.hlk.wbs.espider.applications;

import android.Manifest;

import com.hlk.hlklib.etc.Cryptography;
import com.hlk.wbs.espider.etc.Permission;
import com.hlk.wbs.espider.etc.Utils;
import com.hlk.wbs.espider.helpers.LogHelper;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBaseConfig;

/**
 * <b>功能</b>：缓存数据库相关<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/09 07:07 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class OrmApplication extends BaseApplication {

    public static LiteOrm Orm;

    /**
     * 按照指定的文件名初始化数据库
     */
    public void initializeLiteOrm(String dbName) {
        if (Utils.isEmpty(dbName)) return;

        String db = Utils.getCachePath(Utils.DB_DIR) + Cryptography.md5(dbName) + ".db";
        if (null == Orm) {
            if (initialize(db)) {
                LogHelper.log(TAG, "database initialized at: " + db);
            }
        } else {
            if (!Orm.getDataBaseConfig().dbName.equals(db)) {
                Orm.close();
                if (initialize(db)) {
                    LogHelper.log(TAG, "database re-initialized at: " + db);
                }
            }
        }
    }

    private boolean initialize(String db) {
        if (Permission.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            try {
                Orm = LiteOrm.newSingleInstance(getConfig(db));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 没有权限时，orm = null
            Orm = null;
        }
        return false;
    }

    private DataBaseConfig getConfig(String dbName) {
        DataBaseConfig config = new DataBaseConfig(this, dbName);
        //config.debugged = BuildConfig.DEBUG;
        config.dbVersion = 1;
        return config;
    }
}
