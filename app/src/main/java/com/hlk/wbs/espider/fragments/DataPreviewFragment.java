package com.hlk.wbs.espider.fragments;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.hlk.hlklib.etc.Utility;
import com.hlk.hlklib.inject.ViewId;
import com.hlk.hlklib.inject.ViewUtility;
import com.hlk.wbs.espider.BuildConfig;
import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.activities.base.ToolbarActivity;
import com.hlk.wbs.espider.applications.App;
import com.hlk.wbs.espider.callbacks.OnLiteOrmTaskExecuteListener;
import com.hlk.wbs.espider.etc.Permission;
import com.hlk.wbs.espider.fragments.base.NothingLoadingFragment;
import com.hlk.wbs.espider.models.Position;
import com.hlk.wbs.espider.tasks.OrmTask;
import com.hlk.wbs.tx.custom.CustomConvert;
import com.hlk.wbs.tx.tx10g.Alarm;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * <b>功能</b>：<br />
 * <b>作者</b>：Hsiang Leekwok <br />
 * <b>时间</b>：2016/06/07 01:52 <br />
 * <b>邮箱</b>：xiang.l.g@gmail.com <br />
 */
public class DataPreviewFragment extends NothingLoadingFragment {

    @ViewId(R.id.ui_realm_table_layout)
    private TableLayout mTableLayout;

    List<Position> positions = new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Activity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    protected void destroyView() {

    }

    @Override
    protected void findViews() {
        ViewUtility.bind(this, mView);
    }

    @Override
    protected void getParamsFromBundle(Bundle bundle) {

    }

    @Override
    protected void saveParamsToBundle(Bundle bundle) {

    }

    @Override
    public int getLayout() {
        return R.layout.fragment_temporary_preview;
    }

    @Override
    public void doingInResume() {
        setHasOptionsMenu(BuildConfig.DEBUG);
        Activity().setTitle("Temporary data");
        ((ToolbarActivity) Activity()).showNavigationIcon();
        show();
    }

    @Override
    protected void onDataRefreshed(String className) {
        if (className.equals(Position.class.getName())) {
            // 只刷新位置信息
            refreshPositions();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (BuildConfig.DEBUG) {
            inflater.inflate(R.menu.menu_temporary_data, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.ui_menu_action_temporary_reported:
                showReported = true;
                refreshPositions();
                break;
            case R.id.ui_menu_action_temporary_not_report:
                showReported = false;
                refreshPositions();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshPositions() {
        positions.clear();
        mTableLayout.removeAllViews();
        show();
    }

    private boolean showReported = false;
    private int topBottom = Utility.ConvertDp(3), leftRight = Utility.ConvertDp(5);

    @SuppressWarnings("unchecked")
    private void show() {
        if (positions.size() < 1) {
            new OrmTask<>(Position.class).addOnLiteOrmTaskExecuteListener(new OnLiteOrmTaskExecuteListener<Position>() {
                @Override
                public void onPrepared() {
                    displayLoading(true);
                    displayNothing(false);
                }

                @Override
                public boolean isExecutingWithModify() {
                    return false;
                }

                @Override
                public List<Position> executing(Object object) {
                    if (null == App.Orm) return null;
                    try {
                        if (Permission.hasPermission(Activity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            QueryBuilder<Position> builder = new QueryBuilder<>(Position.class)
                                    .whereEquals(Position.Columns.Report, showReported)
                                    .appendOrderDescBy(Position.Columns.GpsTime).limit(0, 10);
                            return App.Orm.query(builder);
                        }
                        return null;
                    } catch (Exception e) {
                        return null;
                    }
                }

                @Override
                public void progressing(int percentage) {

                }

                @Override
                public void onExecuted(List<Position> result) {
                    if (null != result && result.size() > 0) {
                        positions.addAll(result);
                        // 获取字段名
                        Field[] fields = Position.class.getDeclaredFields();
                        showFields(fields);
                        showTableList(fields, positions);
                    }
                    displayNothing(null == result || result.size() < 1);
                    displayLoading(false);
                }
            }).exec();
        }
    }

    private void showFields(Field[] fields) {
        TableRow row = new TableRow(Activity());
        row.setBackgroundResource(R.color.color_cddc39);
        row.addView(getView("#", 0, true));
        for (Field field : fields) {
            String type = field.getGenericType().toString().toLowerCase();
            int index = type.lastIndexOf('.');
            if (index >= 0) {
                type = type.substring(type.lastIndexOf('.') + 1);
            }
            String string = format("%s(%s)", field.getName(), type);
            row.addView(getView(string, 0, true));
        }
        mTableLayout.addView(row);
    }

    private void showTableList(Field[] fields, List<Position> list) {
        for (int i = 0, len = list.size(); i < len; i++) {
            showLine(fields, list.get(i), i);
        }
        displayLoading(false);
        displayNothing(list.size() < 1);
    }

    private int[] color = new int[]{R.color.color_f9fbe7, R.color.color_f0f4c3, R.color.color_e6ee9c};
    //, R.color.color_dce775, R.color.color_d4e157
    //R.color.color_cddc39, R.color.color_cc0a33, R.color.color_afb42b,
    //R.color.color_9e9d24, R.color.color_827717};

    private int background(int index) {
        int len = color.length;
        int block = len * 2 - 2;// 因为少了2个下标
        int blockSize = index % block;
        int times = (blockSize / len) % 2, mod = blockSize % len;
        return color[times == 0 ? mod : (len - mod - 2)];
    }

    private int text(int index) {
        int len = color.length;
        int times = (index / len) % 2, mod = index % len;
        return times == 0 ? (mod >= 7 ? R.color.color_fffde7 : R.color.color_00a8a8) :
                (len >= 7 && mod <= 1 ? R.color.color_fffde7 : R.color.color_00a8a8);
    }

    private View getView(String text, int index, boolean title) {
        TextView view = new TextView(Activity());
        view.setPadding(topBottom, topBottom, leftRight, topBottom);
        view.setText(String.format("%s", text));
        view.setTextColor(getColor(title ? R.color.color_fffde7 : text(index)));
        return view;
    }

    private void showLine(Field[] fields, Object obj, int index) {
        TableRow row = new TableRow(Activity());
        row.setBackgroundResource(background(index));
        row.addView(getView(format("%d", index), index, false));
        for (Field field : fields) {
            String text = getFieldValue(field, obj);
            row.addView(getView(text, index, false));
        }
        mTableLayout.addView(row);
    }

    private String getFieldValue(Field field, Object obj) {
        Object object = getter(field, obj);
        if (null == object) {
            // 通过get方式获取不到的话，则通过public方式获取
            object = declared(field, obj);
        }
        return String.valueOf(object);
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private static Object getter(Field field, Object object) {

        String fieldName = field.getName().toLowerCase();

        for (Method method : object.getClass().getMethods()) {
            String methodName = method.getName().toLowerCase();
            if (methodName.endsWith("." + fieldName)) {
                String type = field.getGenericType().toString().toLowerCase();
                return value(fieldName, type, invoke(method, object));
            }
        }
        return null;
    }

    private static Object declared(Field field, Object object) {
        String type = field.getGenericType().toString().toLowerCase();
        String fieldName = field.getName().toLowerCase();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field f : fields) {
            String name = f.getName().toLowerCase();
            if (name.equals(fieldName)) {
                f.setAccessible(true);
                try {
                    return value(fieldName, type, f.get(object));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static Object value(String fieldName, String type, Object value) {
        if (type.equals("boolean")) {
            return value;
        } else {
            if (type.equals("long") && fieldName.contains("time")) {
                // 时间格式的内容转换成字符串显示
                return sdf.format(new Date((long) value));
            } else if (type.equals("byte")) {
                String v = "0x" + CustomConvert.byteToHexString((byte) value);
                if (fieldName.contains("alarm")) {
                    // 字节
                    v = v + "(" + Alarm.getAlarm((Byte) value) + ")";
                }
                return v;
            }
        }
        return value;
    }

    private static Object invoke(Method method, Object object) {
        try {
            return method.invoke(object);
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        return null;
    }
}
