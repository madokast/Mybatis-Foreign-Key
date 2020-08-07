package com.github.foreignkey;


import java.util.List;

/**
 * 一个外键约束
 *
 * @author madokast
 * @version 1.0
 */

public class Constraint {

    private String primaryTable;

    private List<String>  primaryKey;

    private String foreignTable;

    private List<String> foreignKey;

    private ConstraintAction onUpdate = ConstraintAction.NO_ACTION;

    private ConstraintAction onDelete = ConstraintAction.NO_ACTION;


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

    @Override
    public String toString() {
        return "Constraint{" +
                "primaryTable='" + primaryTable + '\'' +
                ", primaryKey=" + primaryKey +
                ", foreignTable='" + foreignTable + '\'' +
                ", foreignKey=" + foreignKey +
                ", onUpdate=" + onUpdate +
                ", onDelete=" + onDelete +
                '}';
    }
}
