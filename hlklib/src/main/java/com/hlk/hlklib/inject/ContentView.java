package com.hlk.hlklib.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>功能：</b>提供ContentView的注解<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2015/12/26 08:47 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContentView {
    int value() default -1;
}
