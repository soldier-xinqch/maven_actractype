package com.xinqch.springbootactractype.database.config;

/**
 * 数据源 枚举 接口
 */
public interface DataSourceEnum {

    public enum DATASOURCEKEY implements DataSourceEnum {
        MASTER("master", "主库"),
        SLAVE("slave", "从库");

        private String code;
        private String desc;

        DATASOURCEKEY(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return this.code;
        }

        public String getDesc() {
            return desc;
        }
    }

}
