package com.github.foreignkey;


import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constraint that = (Constraint) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(validation, that.validation) &&
                Objects.equals(primaryTable, that.primaryTable) &&
                Objects.equals(primaryKey, that.primaryKey) &&
                Objects.equals(foreignTable, that.foreignTable) &&
                Objects.equals(foreignKey, that.foreignKey) &&
                onUpdate == that.onUpdate &&
                onDelete == that.onDelete;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, validation, primaryTable, primaryKey, foreignTable, foreignKey, onUpdate, onDelete);
    }
}
