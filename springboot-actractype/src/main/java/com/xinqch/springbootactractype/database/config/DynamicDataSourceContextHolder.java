package com.xinqch.springbootactractype.database.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * 保存当前线程使用的数据源名  数据源路由器
 *
 * @auther xinch
 * @create 2017/12/20 14:20
 */
public class DynamicDataSourceContextHolder {
    public static final Logger log = LoggerFactory.getLogger(DynamicDataSourceContextHolder.class);
    /**
     * 默认数据源
     */
    public static final String DEFAULT_DATASOURCE = DataSourceEnum.DATASOURCEKEY.MASTER.getCode();

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
    /*
     * 管理所有的数据源id;
     * 主要是为了判断数据源是否存在;
     */
    public static Set<Object> dataSourceKeys = new HashSet<Object>();

    /**
     * 设置数据源名
     */
    public static void setDataSourceType(String dbType) {
        log.debug("切换到{}数据源", dbType);
        contextHolder.set(dbType);
    }

    /**
     * 获取数据源名
     *
     * @return
     */
    public static String getDataSourceType() {
        return (contextHolder.get());
    }

    /**
     * 清除数据源名
     */
    public static void clearDataSource() {
        contextHolder.remove();
    }

    /**
     * 判断指定DataSrouce当前是否存在
     *
     * @param dataSourceId
     * @return
     * @author SHANHY
     * @create  2016年1月24日
     */
    public static boolean dataSourceIsExit(String dataSourceId){
        return dataSourceKeys.contains(dataSourceId);
    }
}
