package com.github.foreignkey;


import java.util.List;

/**
 * 一个外键约束
 *
 * @author madokast
 * @version 1.0
 */

public class Constraint {

    private String name;

    private Boolean validation;

    private String primaryTable;

    private List<String> primaryKey;

    private String foreignTable;

    private List<String> foreignKey;

    private ConstraintAction onUpdate;

    private ConstraintAction onDelete;

    /*---------- 以下为自动生成的 sql 语句，由总配置类 ConstraintConfig 负责生成 -------------*/

    /**
     * SELECT COUNT(*) FROM primaryTable WHERE primaryKey[0] = ? [AND primaryKey[1] = ?] LIMIT 1
     * 用于检查主表上是否有对应的主键
     */
    private String sqlSelectFromPrimaryTableByPrimaryKeys;


    /*---------- setter getter -------------*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getValidation() {
        return validation;
    }

    public void setValidation(Boolean validation) {
        this.validation = validation;
    }

    public String getPrimaryTable() {
        return primaryTable;
    }

    public void setPrimaryTable(String primaryTable) {
        this.primaryTable = primaryTable;
    }

    public List<String> getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(List<String> primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getForeignTable() {
        return foreignTable;
    }

    public void setForeignTable(String foreignTable) {
        this.foreignTable = foreignTable;
    }

    public List<String> getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(List<String> foreignKey) {
        this.foreignKey = foreignKey;
    }

    public ConstraintAction getOnUpdate() {
        return onUpdate;
    }

    public void setOnUpdate(ConstraintAction onUpdate) {
        this.onUpdate = onUpdate;
    }

    public ConstraintAction getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(ConstraintAction onDelete) {
        this.onDelete = onDelete;
    }

    public String getSqlSelectFromPrimaryTableByPrimaryKeys() {
        return sqlSelectFromPrimaryTableByPrimaryKeys;
    }

    public void setSqlSelectFromPrimaryTableByPrimaryKeys(String sqlSelectFromPrimaryTableByPrimaryKeys) {
        this.sqlSelectFromPrimaryTableByPrimaryKeys = sqlSelectFromPrimaryTableByPrimaryKeys;
    }

    @Override
    public String toString() {
        return "Constraint{" +
                "name='" + name + '\'' +
                ", validation=" + validation +
                ", primaryTable='" + primaryTable + '\'' +
                ", primaryKey=" + primaryKey +
                ", foreignTable='" + foreignTable + '\'' +
                ", foreignKey=" + foreignKey +
                ", onUpdate=" + onUpdate +
                ", onDelete=" + onDelete +
                '}';
    }
}
