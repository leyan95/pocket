package org.hv.pocket.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wujianchuan 2019/1/3
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {
    /**
     * 对应数据库列明(缺省时根据属性名驼峰转下划线)
     *
     * @return column name
     */
    String name() default "";

    String businessName() default "";

    /**
     * 关键业务
     *
     * @return flag business
     */
    boolean flagBusiness() default false;
}
