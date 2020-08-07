package com.github.foreignkey;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description
 * 完整外界约束配置类
 *
 * @author madokast
 * @version 1.0
 */

public class ConstraintConfig {

    private Map<String, Constraint> primaryTableMap = new ConcurrentHashMap<>();

    private Map<String, Constraint> foreignTableMap = new ConcurrentHashMap<>();

    public void addConstraint(Constraint constraint) {
        primaryTableMap.put(constraint.getPrimaryTable(), constraint);
        foreignTableMap.put(constraint.getForeignTable(), constraint);
    }

    @Override
    public String toString() {
        return "ConstraintConfig{" +
                "primaryTableMap=" + primaryTableMap +
                ", foreignTableMap=" + foreignTableMap +
                '}';
    }
}
