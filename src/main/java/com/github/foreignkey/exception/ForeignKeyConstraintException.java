package com.github.foreignkey.exception;


import com.github.foreignkey.Constraint;

/**
 * 外键约束异常
 * Cannot add or update a child row: a foreign key constraint fails (`a-b`.`t2`, CONSTRAINT `t2_ibfk_1` FOREIGN KEY (`c2`) REFERENCES `a-b`.`t1` (`c1`))
 * insert into t2 values(1,1)
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
