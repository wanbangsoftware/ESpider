package com.hlk.hlklib.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>功能：</b>提供FindViewById的注解<br />
 * 如果是本view，可以将注解的成员定义成私有；<br />如果要注解父类的成员，其父类的成员必须设置成public<br />
 * <b>作者：</b>Hsiang Leekwok <br />
 * <b>时间：</b>2015/12/26 08:49 <br />
 * <b>邮箱：</b>xiang.l.g@gmail.com <br />
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewId {
    int value() default -1;
}
