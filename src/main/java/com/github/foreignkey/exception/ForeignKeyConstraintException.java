package com.github.foreignkey.exception;


import com.github.foreignkey.Constraint;

/**
 * 违反外键约束异常
 *
 * @author madokast
 * @version 1.0
 */

public class ForeignKeyConstraintException extends RuntimeException {

    public ForeignKeyConstraintException(String message) {
        super(message);
    }

    public ForeignKeyConstraintException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ForeignKeyConstraintException insertToForeignTableFailed(Constraint constraint, Object insertObject) {
        if (insertObject == null) {
            insertObject = "";
        }
        return new ForeignKeyConstraintException(
                "Cannot add a child row: a foreign key constraint fails" +
                        String.format(
                                " (CONSTRAINT `%s` FOREIGN KEY `%s`(`%s`) REFERENCES `%s`(`%s`))",
                                constraint.getName(),
                                constraint.getForeignTable(),
                                String.join(",", constraint.getForeignKey()),
                                constraint.getPrimaryTable(),
                                String.join(",", constraint.getPrimaryKey())
                        ) +
                        String.format(
                                " insert into `%s` object(%s)",
                                constraint.getForeignTable(),
                                insertObject.toString()
                        )
        );
    }
}
