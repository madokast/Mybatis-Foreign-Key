package com.github.foreignkey.utils;


import com.github.foreignkey.Constraint;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description
 * 根据 {@link com.github.foreignkey.Constraint} 生成 sql
 * <p>
 *
 * @author madokast
 * @version 1.0
 */

public class ConstraintSQLGenerator {

    // 缓存无参 sql 语句
    Map<CacheKey, String> cache = new ConcurrentHashMap<>();

    /**
     * SELECT COUNT(*) FROM primaryTable WHERE primaryKey[0] = ? [AND primaryKey[1] = ?] LIMIT 1
     * 用于检查主表上是否有对应的主键
     *
     * @param constraint 约束
     * @return sql语句
     */
    public String selectCountFromPrimaryTablePrimaryKeys(Constraint constraint) {
        CacheKey cacheKey = new CacheKey(constraint, "selectCountFromPrimaryTablePrimaryKeys");
        String sql = cache.get(cacheKey);

        if (sql == null) {
            SQL s = new SQL().SELECT("COUNT(*)").FROM(StringUtils.addUnquote(constraint.getPrimaryTable()));
            for (String pk : constraint.getPrimaryKey()) {
                s.WHERE(StringUtils.addUnquote(pk) + " = ? ");
            }
            s.LIMIT(1);
            sql = s.toString();
            cache.put(cacheKey, sql);
        }


        return sql;
    }

    /**
     * 方法含义见 {@link ConstraintSQLGenerator#selectCountFromPrimaryTablePrimaryKeys(Constraint)}
     * 当 params 还有对应的 key = 列名时，使用 value，否则 ?
     *
     * @param constraint 约束
     * @param params     列名-value
     * @return sql语句
     */
    public String selectCountFromPrimaryTablePrimaryKeys(Constraint constraint, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return selectCountFromPrimaryTablePrimaryKeys(constraint);
        }

        SQL s = new SQL().SELECT("COUNT(*)").FROM(StringUtils.addUnquote(constraint.getPrimaryTable()));
        for (String pk : constraint.getPrimaryKey()) {
            s.WHERE(StringUtils.addUnquote(pk) + " = " + params.getOrDefault(pk, "?") + " ");
        }
        s.LIMIT(1);
        return s.toString();
    }


    // 缓存用 key，仅仅缓存没有 params 的sql语句
    static class CacheKey {
        Constraint constraint;
        String sqlType;

        public CacheKey(Constraint constraint, String sqlType) {
            this.constraint = constraint;
            this.sqlType = sqlType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(constraint, cacheKey.constraint) &&
                    Objects.equals(sqlType, cacheKey.sqlType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(constraint, sqlType);
        }
    }
}
