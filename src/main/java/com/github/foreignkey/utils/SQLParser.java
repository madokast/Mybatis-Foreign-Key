package com.github.foreignkey.utils;


import com.github.foreignkey.UpdateType;
import com.github.foreignkey.exception.SQLParseException;
import com.github.foreignkey.sql.InsertSQL;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description
 * SQL 工具类
 * 主要是封装了 jsqlparser
 * <p>
 *
 * @author madokast
 * @version 1.0
 */

public class SQLParser { // 遵循传统 SQL 三个字大写

    public UpdateType resolveUpdateType(String sql) {
        sql = sql.trim().substring(0, 8).toUpperCase();
        if (sql.startsWith("INSERT")) return UpdateType.INSERT;
        if (sql.startsWith("UPDATE")) return UpdateType.UPDATE;
        if (sql.startsWith("DELETE")) return UpdateType.DELETE;
        if (sql.startsWith("REPLACE")) return UpdateType.REPLACE;


        throw new SQLParseException("sql[" + sql + "] is not an updating statement");
    }

    /**
     * 解析 insert SQL 语句
     * @param insertSQL insert SQL 语句
     * @return 解析生成 InsertSQL 对象
     */
    public InsertSQL parseInsertSQL(String insertSQL){
        Insert insert;
        try {
            insert = (Insert) CCJSqlParserUtil.parse(insertSQL);
        } catch (JSQLParserException e) {
            throw new SQLParseException("cannot parse insert SQL " + insertSQL, e);
        }

        String tableNme = StringUtils.dropUnquote(insert.getTable().getName());
        List<String> columns = insert.getColumns().stream().map(Column::getColumnName)
                .map(StringUtils::dropUnquote).collect(Collectors.toList());
        List<Expression> expressions = ((ExpressionList) insert.getItemsList()).getExpressions();// JdbcParameters

        List<String> values = expressions.stream().map(exp->{
            if(exp instanceof JdbcParameter){
                return "?";
            }else {
                return exp.toString();
            }
        }).collect(Collectors.toList());

        return new InsertSQL(insertSQL,tableNme,columns,values);
    }
}
