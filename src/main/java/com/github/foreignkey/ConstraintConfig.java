package com.github.foreignkey;


import com.github.foreignkey.exception.ConfigException;
import org.apache.ibatis.jdbc.SQL;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 * 完整外界约束配置类
 *
 * 自动生成判断约束的 SQL
 *
 * 情况一：
 *      从表执行 INSERT ，首先需要在主表上查看有没有对应的主键记录
 *      因此需要执行SQL: SELECT COUNT(*) FROM primaryTable WHERE primaryKey[0] = ? [AND primaryKey[1] = ?] LIMIT 1
 *      检查返回值是不是空，如果空，则说明违反一致性约束
 * </pre>
 *
 * @author madokast
 * @version 1.0
 */

public class ConstraintConfig {

    private Map<String, List<Constraint>> primaryTableMap = new ConcurrentHashMap<>();

    private Map<String, List<Constraint>> foreignTableMap = new ConcurrentHashMap<>();


    public void addConstraint(Constraint constraint) {
        validate(constraint);
        generateSql(constraint);

        String primaryTable = constraint.getPrimaryTable();
        String foreignTable = constraint.getForeignTable();

        primaryTableMap.putIfAbsent(primaryTable, new ArrayList<>());
        foreignTableMap.putIfAbsent(foreignTable, new ArrayList<>());

        primaryTableMap.get(primaryTable).add(constraint);
        foreignTableMap.get(foreignTable).add(constraint);
    }

    @SuppressWarnings("unchecked")
    public List<Constraint> getForeignConstraints(String table) {
        return foreignTableMap.getOrDefault(table, Collections.EMPTY_LIST);
    }

    /**
     * 生成验证完整性所需要的 sql
     * 嫌麻烦，就不懒加载了
     */
    private void generateSql(Constraint constraint) {
        String primaryTable = constraint.getPrimaryTable();
        List<String> primaryKey = constraint.getPrimaryKey();

        // SELECT COUNT(*) FROM primaryTable WHERE primaryKey[0] = ? [AND primaryKey[1] = ?] LIMIT 1
        SQL sql = new SQL().SELECT("COUNT(*)").FROM(primaryTable);
        for (String pk : primaryKey) {
            sql.WHERE(pk + " = ? ");
        }
        sql.LIMIT(1);

        constraint.setSqlSelectFromPrimaryTableByPrimaryKeys(sql.toString());
    }

    /**
     * constraint not null
     * 验证 constraint
     */
    private void validate(Constraint constraint) {
        List<String> primaryKey = constraint.getPrimaryKey();
        List<String> foreignKey = constraint.getForeignKey();

        try {
            Objects.requireNonNull(constraint.getPrimaryTable());
            Objects.requireNonNull(primaryKey);
            Objects.requireNonNull(constraint.getForeignTable());
            Objects.requireNonNull(foreignKey);
            Objects.requireNonNull(constraint.getOnDelete());
            Objects.requireNonNull(constraint.getOnUpdate());
        } catch (Exception e) {
            throw new ConfigException("error in config [" + constraint + "]", e);
        }

        int ps = primaryKey.size();
        int fs = foreignKey.size();

        if (ps == 0) throw new ConfigException("error in config [" + constraint + "], primary key is empty");
        if (fs == 0) throw new ConfigException("error in config [" + constraint + "], foreign key is empty");

        if (ps != fs)
            throw new ConfigException("error in config [" + constraint + "], sizes of primary key and foreign key are inconsistent");
    }

    @Override
    public String toString() {
        return "ConstraintConfig{" +
                "primaryTableMap=" + primaryTableMap +
                ", foreignTableMap=" + foreignTableMap +
                '}';
    }

}
