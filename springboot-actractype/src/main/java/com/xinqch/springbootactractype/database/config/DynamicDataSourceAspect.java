package com.xinqch.springbootactractype.database.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * AOP切面 实现动态切换数据源逻辑
 *
 * @auther xinch
 * @create 2017/12/20 14:43
 */
@Aspect
// 该切面应当先于 @Transactional 执行
@Order(-1)
@Component
public class DynamicDataSourceAspect {
    public static final Logger logger = LoggerFactory.getLogger(DynamicDataSourceAspect.class);

    @Before("@annotation(targetDataSource)")
    public void changeDataSource(JoinPoint point,DataSourceTarget targetDataSource) {
        if (!DynamicDataSourceContextHolder.dataSourceIsExit(targetDataSource.value())) {
            logger.error("DataSource [{}] doesn't exist, use default DataSource [{}]", targetDataSource.value());
        } else {
            // 切换数据源
            DynamicDataSourceContextHolder.setDataSourceType(targetDataSource.value());
            logger.info("Switch DataSource to [{}] in Method [{}]",
                    DynamicDataSourceContextHolder.getDataSourceType(), point.getSignature());
        }
    }

    @Before("@annotation(targetDataSource)")
    public void afterSwitchDS(JoinPoint point,DataSourceTarget targetDataSource) {
        logger.debug("Revert DataSource : {} > {}", targetDataSource.value(), point.getSignature());
        //方法执行完毕之后，销毁当前数据源信息，进行垃圾回收。 将数据源置为默认数据源
        DynamicDataSourceContextHolder.clearDataSource();
        logger.info("Restore DataSource to [{}] in Method [{}]",
                DynamicDataSourceContextHolder.getDataSourceType(), point.getSignature());
    }
}
