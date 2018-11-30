package com.github.linshenkx.annotation;

import java.lang.annotation.*;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-28
 * @Description: 标记扫描注解，使用该注解的类会被 BeanFactory 收入管理
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    String value() default "";
}
