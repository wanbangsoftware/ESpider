package com.hlk.hlklib.inject;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;

import com.hlk.hlklib.R;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <b>功能：</b>提供findViewById注解解释<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2015/12/26 08:51 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
public final class ViewUtility {

    private static final String TAG = "ViewUtility";

    public static void bind(Object object) {
        if (object instanceof Activity)
            bind((Activity) object);
        else if (object instanceof Fragment)
            bind((Fragment) object);
        else if (object instanceof View)
            bind((View) object);
    }

    public static void bind(Activity activity) {
        bind(activity, activity);
    }

    public static void bind(Fragment fragment) {
        bind(fragment, fragment);
    }

    public static void bind(View view) {
        bind(view, view);
    }

    public static void bind(Object object, Activity activity) {
        View view = activity.getWindow().getDecorView().findViewById(android.R.id.content);//.getChildAt(0);
        injectViewById(object, view);
    }

    public static void bind(Object object, Fragment fragment) {
        injectViewById(object, fragment.getView());
    }

    public static void bind(Object object, View target) {
        injectViewById(object, target);
    }

    private static void injectViewById(Object object, View target) {
        if (null == target) return;

        Class<?> classType = object.getClass();
        /**
         * 返回 Field 对象的一个数组，这些对象反映此 Class 对象表示的类或接口声明的成员变量，<br />
         * 包括公共、保护、默认（包）访问和私有成员变量，但不包括继承的成员变量。
         * */
        // 自有成员变量
        injectView(object, target, classType.getDeclaredFields());
        // 父类的public成员变量
        injectView(object, target, classType.getFields());

        injectViewEvent(object, target);
    }

    private static void injectView(Object object, View target, Field[] fields) {
        if (fields.length > 0) {
            for (Field field : fields) {
                // 该成员变量是否存在ViewId类型的注解
                if (field.isAnnotationPresent(ViewId.class)) {
                    ViewId annotation = field.getAnnotation(ViewId.class);
                    int viewId = annotation.value();
                    View view = target.findViewById(viewId);
                    if (null != view) {
                        try {
                            field.setAccessible(true);
                            field.set(object, view);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 绑定事件
     */
    private static void injectViewEvent(Object object, View target) {
        Class<?> cls = object.getClass();
        // 私有方法
        injectViewEvent(object, target, cls.getDeclaredMethods());
        // 父类的公共方法
        injectViewEvent(object, target, cls.getMethods());
    }

    private static void injectViewEvent(Object object, View target, Method[] methods) {
        for (Method method : methods) {
            Click click = method.getAnnotation(Click.class);
            if (null != click) {
                int[] ids = click.value();
                if (ids.length > 0) {
                    ClickEvent event = new ClickEvent(object);
                    for (int id : ids) {
                        event.setClickEvent(id, method.getName());
                        View view = target.findViewById(id);
                        view.setOnClickListener(event);
                        view.setTag(R.id.hlklib_ids_custom_view_click_tag, event);
                    }
                }
            }
        }
    }

    /**
     * 重置相应的method绑定
     */
    public static void reset(Object object) {
        if (object instanceof Activity) {
            reset(object, ((Activity) object).getWindow().getDecorView().findViewById(android.R.id.content));
        } else if (object instanceof Fragment) {
            reset(object, ((Fragment) object).getView());
        } else if (object instanceof View) {
            reset(object, (View) object);
        }
    }

    private static void reset(Object object, View target) {
        Class<?> cls = object.getClass();
        reset(target, cls.getMethods());
        reset(target, cls.getDeclaredMethods());
    }

    private static void reset(View target, Method[] methods) {
        for (Method method : methods) {
            Click click = method.getAnnotation(Click.class);
            if (null != click) {
                int[] ids = click.value();
                if (ids.length > 0) {
                    for (int id : ids) {
                        View v = target.findViewById(id);
                        ClickEvent event = (ClickEvent) v.getTag(R.id.hlklib_ids_custom_view_click_tag);
                        v.setOnClickListener(null);
                        event.removeClickEvent(id);
                    }
                }
            }
        }
    }

    private static void injectContentView(Object object) {
        Class<?> classType = object.getClass();
        // 该类是否存在ContentView类型的注解
        if (classType.isAnnotationPresent(ContentView.class)) {
            // 返回存在的ContentView类型的注解
            ContentView annotation = classType.getAnnotation(ContentView.class);
            try {
                // 返回一个 Method 对象，它反映此 Class 对象所表示的类或接口的指定公共成员方法。
                Method method = classType.getMethod("setContentView", int.class);
                method.setAccessible(true);
                int resId = annotation.value();
                method.invoke(object, resId);


            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
