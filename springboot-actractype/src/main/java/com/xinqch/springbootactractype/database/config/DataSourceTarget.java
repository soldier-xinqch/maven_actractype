package com.xinqch.springbootactractype.database.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定使用那个数据源
 *
 * @author xinqch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.METHOD,
        ElementType.TYPE
})
public @interface DataSourceTarget {

    String value();
}
