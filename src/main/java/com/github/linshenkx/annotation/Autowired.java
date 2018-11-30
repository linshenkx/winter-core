package com.github.linshenkx.annotation;

import java.lang.annotation.*;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-28
 * @Description: 依赖注入的注解，目前只实现 Field 注入
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    boolean required() default true;
}
