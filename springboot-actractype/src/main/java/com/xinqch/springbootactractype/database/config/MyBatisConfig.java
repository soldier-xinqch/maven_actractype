package com.xinqch.springbootactractype.database.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * mybatis配置类
 *
 * @auther xinch
 * @create 2017/12/21 14:21
 */
@Configuration
@EnableTransactionManagement
public class MyBatisConfig extends AbstractDruidDBConfig{

    @Bean(name = "master_datasource", initMethod = "init", destroyMethod = "close")
    @ConfigurationProperties(prefix = "spring.datasource.master") // application.properteis中对应属性的前缀
    public DruidDataSource dataSource_master() {
        return super.createDataSource();
    }

    @Bean(name = "master_datasource", initMethod = "init", destroyMethod = "close")
    @ConfigurationProperties(prefix = "spring.datasource.slave") // application.properteis中对应属性的前缀
    public DruidDataSource dataSource_slave() {
        return super.createDataSource();
    }

    /**
     * 动态数据源: 通过AOP在不同数据源之间动态切换
     * @return
     */
    @Bean(name = "dynamicDS1")
    public DynamicDataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        // 默认数据源
        dynamicDataSource.setDefaultTargetDataSource(dataSource_master());

        // 配置多数据源
        Map<Object, Object> dataSourceMap = new HashMap(5);
        dataSourceMap.put(DataSourceEnum.DATASOURCEKEY.MASTER.getCode(), dataSource_master());
        dataSourceMap.put(DataSourceEnum.DATASOURCEKEY.SLAVE.getCode(), dataSource_slave());
        // 将 master 和 slave 数据源作为指定的数据源
        dynamicDataSource.setTargetDataSources(dataSourceMap);
        // 将数据源的 key 放到数据源上下文的 key 集合中，用于切换时判断数据源是否有效
        DynamicDataSourceContextHolder.dataSourceKeys.addAll(dataSourceMap.keySet());
        return dynamicDataSource;
    }

    @Bean(name = "sqlSessionFactorYmqOne")
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        return super.sqlSessionFactory(dynamicDataSource());
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws SQLException {
        return new DataSourceTransactionManager(dynamicDataSource());
    }
}
