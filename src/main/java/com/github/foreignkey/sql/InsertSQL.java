package com.github.foreignkey.sql;


import java.util.List;

/**
 * Description
 * 被解析的 insert sql 语句
 * <p>
 *
 * @author madokast
 * @version 1.0
 */

public class InsertSQL {

    /**
     * 原 sql 语句
     */
    private String sql;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 列名
     * 和 values 一一对应
     */
    private List<String> columnNames;

    /**
     * 参数值
     * 和列名一一对应
     * 例如 ? 'r01'
     */
    private List<String> values;

    public InsertSQL(String sql, String tableName, List<String> columnNames, List<String> values) {
        this.sql = sql;
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.values = values;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "InsertSQL{" +
                "sql='" + sql + '\'' +
                ", tableName='" + tableName + '\'' +
                ", columnNames=" + columnNames +
                ", values=" + values +
                '}';
    }
}
