package com.github.linshenkx.annotation;

import java.lang.annotation.*;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-28
 * @Description: 指定名称用的注解，跟@Autowired搭配使用，实现在类型相同的情况下通过指定名识别
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Qualifier {
    String value() default "";

}
