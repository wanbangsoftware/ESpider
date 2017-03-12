package com.hlk.hlklib.inject;

import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;

import java.lang.reflect.Method;

/**
 * <b>功能：</b><br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2015/12/26 10:05 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public class ClickEvent implements OnClickListener {

    private SparseArray<String> listeners = new SparseArray<String>();

    private Object instance;
    private Class<?> clss;

    public ClickEvent(Object object) {
        instance = object;
        clss = instance.getClass();
    }

    public void setClickEvent(int id, String methodName) {
        listeners.put(id, methodName);
    }

    public void removeClickEvent(int id) {
        listeners.remove(id);
    }

    @Override
    public void onClick(View v) {
        if (listeners == null || listeners.size() <= 0) {
            return;
        }
        String methodName = listeners.get(v.getId());
        if (TextUtils.isEmpty(methodName)) {
            return;
        }
        try {
            //通过类去查找对应的方法以及参数类型
            Method callbackMethod;
            try {
                // 查找自有方法
                callbackMethod = clss.getDeclaredMethod(methodName, View.class);
            } catch (Exception e) {
                // 查找父类的公共方法
                callbackMethod = clss.getMethod(methodName, View.class);
            }
            // 自有方法没有时，查找父类的公共方法
            callbackMethod.setAccessible(true);
            //传递对应的参数过去
            callbackMethod.invoke(instance, v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
