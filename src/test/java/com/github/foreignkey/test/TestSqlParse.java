package com.github.foreignkey.test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Description
 * 测试 sql 解析器
 * <p>
 * INSERT 语句格式
 * --  INSERT INTO 表名称 VALUES (值1, 值2,....)
 * --  INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)
 *
 * <p>
 *
 * @author madokast
 * @version 1.0
 */

public class TestSqlParse {

    private final Log LOGGER = LogFactory.getLog(TestSqlParse.class);

    @Test
    public void parseInsertTest01() throws JSQLParserException {
        String sql = "INSERT INTO `tb_user`(`id`,`username`) VALUES (1+1+1, 'abc')";

        Insert insert = (Insert) CCJSqlParserUtil.parse(sql);

        Table table = insert.getTable();

        List<Column> columns = insert.getColumns();

        Assert.assertEquals("tb_user", dropUnquote(table.getName()));

        Assert.assertEquals("id", dropUnquote(columns.get(0).getColumnName()));

        Assert.assertEquals("username", dropUnquote(columns.get(1).getColumnName()));
    }

    String dropUnquote(String str) {
        if (str.startsWith("`") && str.endsWith("`"))
            return str.substring(1, str.length() - 1);

        return str;
    }

    @Test
    public void parseInsertTest02() throws JSQLParserException {
        String sql = "INSERT INTO `tb_user` VALUES (?,?)";

        Insert insert = (Insert) CCJSqlParserUtil.parse(sql);

        Table table = insert.getTable();

        List<Column> columns = insert.getColumns();

        Assert.assertEquals("tb_user", dropUnquote(table.getName()));

        Assert.assertNull(columns);
    }
}
