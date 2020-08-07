package com.github.foreignkey;


import java.sql.SQLException;

/**
 * update 语句的类型
 *
 * @author madokast
 * @version 1.0
 */

public enum UpdateType {
    INSERT, UPDATE, DELETE, REPLACE;

    public static UpdateType resolve(String sql) {
        sql = sql.trim().substring(0, 8).toUpperCase();
        if (sql.startsWith("INSERT")) return INSERT;
        if (sql.startsWith("UPDATE")) return UPDATE;
        if (sql.startsWith("DELETE")) return DELETE;
        if (sql.startsWith("REPLACE")) return REPLACE;

        // 见鬼了
        throw new RuntimeException(new SQLException("sql[" + sql + "] is not an updating statement"));
    }
}
