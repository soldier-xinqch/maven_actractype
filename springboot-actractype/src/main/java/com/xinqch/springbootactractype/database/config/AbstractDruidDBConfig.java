package com.xinqch.springbootactractype.database.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.plugins.PerformanceInterceptor;
import com.baomidou.mybatisplus.plugins.parser.ISqlParser;
import com.baomidou.mybatisplus.plugins.parser.ISqlParserFilter;
import com.baomidou.mybatisplus.plugins.parser.tenant.TenantHandler;
import com.baomidou.mybatisplus.plugins.parser.tenant.TenantSqlParser;
import com.baomidou.mybatisplus.toolkit.PluginUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建数据源基类
 *
 */
@Configuration
@EnableConfigurationProperties(DruidDbProperties.class)
@Import({DruidMonitConfig.class})
public abstract class AbstractDruidDBConfig {


    private Logger logger = LoggerFactory.getLogger(AbstractDruidDBConfig.class);

    public DruidDataSource createDataSource() {
        DruidDbProperties druidDbProperties = new DruidDbProperties();
        if (StringUtils.isEmpty(druidDbProperties.getUrl())) {
            System.out.println(
                    "Your database connection pool configuration is incorrect!" + " Please check your Spring profile");
            throw new ApplicationContextException("Database connection pool is not configured correctly");
        }

        DruidDataSource datasource = new DruidDataSource();

        datasource.setUrl(druidDbProperties.getUrl());
        datasource.setUsername(druidDbProperties.getUsername());
        datasource.setPassword(druidDbProperties.getPassword());
        // datasource.setDriverClassName(
        // StringUtils.isEmpty(driverClassName) ?
        // druidDbProperties.getDriverClassName() : driverClassName);
        datasource.setInitialSize(druidDbProperties.getInitialSize());
        datasource.setMinIdle(druidDbProperties.getMinIdle());
        datasource.setMaxActive(druidDbProperties.getMaxActive());
        datasource.setMaxWait(druidDbProperties.getMaxWait());
        datasource.setTimeBetweenEvictionRunsMillis(druidDbProperties.getTimeBetweenEvictionRunsMillis());
        datasource.setMinEvictableIdleTimeMillis(druidDbProperties.getMinEvictableIdleTimeMillis());
        datasource.setValidationQuery(druidDbProperties.getValidationQuery());
        datasource.setTestWhileIdle(druidDbProperties.isTestWhileIdle());
        datasource.setTestOnBorrow(druidDbProperties.isTestOnBorrow());
        datasource.setTestOnReturn(druidDbProperties.isTestOnReturn());
        try {
            datasource.setFilters(druidDbProperties.getFilters());
        } catch (SQLException e) {
            logger.error("druid configuration initialization filter", e);
        }
        datasource.setConnectionProperties(druidDbProperties.getConnectionProperties());
        return datasource;

    }

    /**
     * 加载默认mybatis xml配置文件，并初始化分页插件
     *
     * @param dataSource
     * @return
     * @throws Exception
     */
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        return createSqlSessionFactory(dataSource, "classpath:mybatis/**/*.xml");
    }

    /**
     * 加载mybatis xml配置文件，并初始化分页插件
     *
     * @param dataSource      数据源
     * @param mapperLocations 自定义xml配置路径
     * @return
     * @throws Exception
     */
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, String mapperLocations) throws Exception {
        return createSqlSessionFactory(dataSource, mapperLocations);
    }

    /**
     * mybatis-plus SQL执行效率插件【生产环境可以关闭】
     */
//    @Bean
    private PerformanceInterceptor performanceInterceptor() {
        return new PerformanceInterceptor();
    }

    /**
     * mybatis-plus分页插件<br>
     * 文档：http://mp.baomidou.com<br>
     */
    private PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        paginationInterceptor.setLocalPage(true);// 开启 PageHelper 的支持
        paginationInterceptor.setDialectType("mysql");
        /*
         * 【测试多租户】 SQL 解析处理拦截器<br>
         * 这里固定写成住户 1 实际情况你可以从cookie读取，因此数据看不到 【 麻花藤 】 这条记录（ 注意观察 SQL ）<br>
         */
        List<ISqlParser> sqlParserList = new ArrayList<>();
        TenantSqlParser tenantSqlParser = new TenantSqlParser();
        tenantSqlParser.setTenantHandler(new TenantHandler() {
            @Override
            public Expression getTenantId() {
                return new LongValue(1L);
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            @Override
            public boolean doTableFilter(String tableName) {
                // 这里可以判断是否过滤表
                /*
                if ("user".equals(tableName)) {
                    return true;
                }*/
                return false;
            }
        });
        sqlParserList.add(tenantSqlParser);
        paginationInterceptor.setSqlParserList(sqlParserList);
        paginationInterceptor.setSqlParserFilter(new ISqlParserFilter() {
            @Override
            public boolean doFilter(MetaObject metaObject) {
                MappedStatement ms = PluginUtils.getMappedStatement(metaObject);
                // 过滤自定义查询此时无租户信息约束【 麻花藤 】出现
                if ("com.baomidou.springboot.mapper.UserMapper.selectListBySQL".equals(ms.getId())) {
                    return true;
                }
                return false;
            }
        });
        return paginationInterceptor;
    }

//            | 全局配置设置
//            | 可配置选项                   默认值,     描述
//            | aggressiveLazyLoading       true,     当设置为‘true’的时候，懒加载的对象可能被任何懒属性全部加载。否则，每个属性都按需加载。
//            | multipleResultSetsEnabled   true,     允许和不允许单条语句返回多个数据集（取决于驱动需求）
//            | useColumnLabel              true,     使用列标签代替列名称。不同的驱动器有不同的作法。参考一下驱动器文档，或者用这两个不同的选项进行测试一下。
//            | useGeneratedKeys            false,    允许JDBC 生成主键。需要驱动器支持。如果设为了true，这个设置将强制使用被生成的主键，有一些驱动器不兼容不过仍然可以执行。
//            | autoMappingBehavior         PARTIAL,  指定MyBatis 是否并且如何来自动映射数据表字段与对象的属性。PARTIAL将只自动映射简单的，没有嵌套的结果。FULL 将自动映射所有复杂的结果。
//            | defaultExecutorType         SIMPLE,   配置和设定执行器，SIMPLE 执行器执行其它语句。REUSE 执行器可能重复使用prepared statements 语句，BATCH执行器可以重复执行语句和批量更新。
//            | defaultStatementTimeout     null,     设置一个时限，以决定让驱动器等待数据库回应的多长时间为超时
    private SqlSessionFactory createSqlSessionFactory(DataSource dataSource, String mapperLocations) throws Exception {

        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        //对于位置的sql查询，允许返回不同的结果及一道道通用的结果
        configuration.setMultipleResultSetsEnabled(true);
        //配置默认的执行器，SIMPLE 执行器没有什么特别之处 REUSE
        configuration.setDefaultExecutorType(ExecutorType.REUSE);
        //全局启用或禁用延迟加载，当禁用时，所有关联对象都会即时加载
        configuration.setLazyLoadingEnabled(false);
        configuration.setAggressiveLazyLoading(true);
        //设置超时时间，他决定驱动等待一个数据库响应的时间
        configuration.setDefaultStatementTimeout(30000);
        configuration.setUseColumnLabel(true);

        sqlSessionFactoryBean.setConfiguration(configuration);
        // mybatis分页
        Interceptor[] interceptors = new Interceptor[]{};
        //分页插件
        interceptors[0] = paginationInterceptor();
        // sql 执行效率
        interceptors[1] = performanceInterceptor();

        sqlSessionFactoryBean.setPlugins(interceptors);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources(mapperLocations));
        return sqlSessionFactoryBean.getObject();

    }

//    private GlobalConfiguration setGlobalConfiguration(){
//        GlobalConfiguration globalConfiguration = new GlobalConfiguration();
//        // ID 策略 default 0 （数据库ID自增） 1 ID_work 用户输入 2 全局唯一ID 3 UUID 全局唯一ID
//        globalConfiguration.setIdType(2);
//        globalConfiguration.setDbType(DBType.MYSQL.name());
//        return globalConfiguration;
//    }
}
