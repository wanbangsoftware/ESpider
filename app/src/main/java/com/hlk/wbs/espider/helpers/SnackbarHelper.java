package com.hlk.wbs.espider.helpers;

import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.hlk.wbs.espider.R;
import com.hlk.wbs.espider.applications.App;

/**
 * 提供新版Snackbar提示<br />
 * Created by Hsiang Leekwok on 2015/12/14.
 */
public class SnackbarHelper {

    public enum ShowType {
        None, Warning, Error
    }

    private static int showTypeColor(ShowType showType) {
        int res;
        switch (showType) {
            case Error:
                res = R.color.color_f57f17;
                break;
            case Warning:
                res = R.color.color_fbc02d;
                break;
            default:
                res = R.color.color_fffde7;
                break;
        }
        return App.getInstance().getResources().getColor(res);
    }

    /**
     * 通过Snackbar显示提示内容
     */
    public static void show(View trigger, String msg, ShowType showType) {
        Snackbar snackbar = Snackbar.make(trigger, msg, Snackbar.LENGTH_LONG);
        TextView text = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        text.setTextColor(showTypeColor(showType));
        snackbar.show();
    }
}
