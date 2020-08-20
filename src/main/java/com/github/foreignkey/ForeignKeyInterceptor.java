package com.github.foreignkey;


import com.github.foreignkey.exception.ConfigException;
import com.github.foreignkey.exception.ForeignKeyConstraintException;
import com.github.foreignkey.exception.SQLParseException;
import com.github.foreignkey.sql.InsertSQL;
import com.github.foreignkey.utils.ConstraintSQLGenerator;
import com.github.foreignkey.utils.SQLParser;
import com.github.foreignkey.utils.StringUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.RowBounds;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description
 * 施加外键约束的拦截器
 * <p>
 *
 * @author madokast
 * @version 1.0
 */

@Intercepts({@Signature(
        type = Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class}
)})
public class ForeignKeyInterceptor implements Interceptor {

    private final Log LOGGER = LogFactory.getLog(ForeignKeyInterceptor.class);

    private ConstraintSQLGenerator sqlGenerator = new ConstraintSQLGenerator();

    private SQLParser sqlParser = new SQLParser();

    private ConstraintConfig constraintConfig;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Executor executor = (Executor) invocation.getTarget();

        Object[] args = invocation.getArgs();

        // MappedStatement 对象代表一个配置的 sql 信息，如 xml配置的<select>或者注解配置的 @Select
        MappedStatement ms = (MappedStatement) args[0];

        // mapper 方法中入参
        Object parameter = args[1];

        // 对 sqlSource中 sql和 parameter 参数的封装
        // 一般可以认为：sqlSource + mapper方法的入参 = BoundSql
        BoundSql boundSql = ms.getBoundSql(parameter);

        // 拿到 sql 语句，这时语句中的 #{} 已经换为 ?
        String sql = boundSql.getSql();
        LOGGER.debug("拦截到 sql=" + sql);

        // sql 中 ? 和 parameter 属性的映射
        // 如 sql 中第一个 ? 对应 ((User)parameter).id
        // 注意：parameterMappings 是一个 list，顺序是很重要的，list 顺序就是 SQL 语句中 ? 的顺序
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        LOGGER.debug("sql 参数映射=" + parameterMappings);
        LOGGER.debug("sql 参数=" + parameter);

        // 分析 sql 语句类型 insert update delete replace
        switch (sqlParser.resolveUpdateType(sql)) {
            case INSERT:
                // 判断插入是不是在从表中进行
                InsertSQL insertSQL = sqlParser.parseInsertSQL(sql);
                String tableName = insertSQL.getTableName();
                List<String> foreignTableColumnNames = insertSQL.getColumnNames();
                List<String> foreignTableValues = insertSQL.getValues();
//                Insert insert = (Insert) CCJSqlParserUtil.parse(sql);
//                String table = StringUtils.dropUnquote(insert.getTable().getName());
//                List<String> columns = insert.getColumns().stream().map(Column::getColumnName).map(StringUtils::dropUnquote).collect(Collectors.toList());
//                List<Expression> expressions = ((ExpressionList) insert.getItemsList()).getExpressions();// JdbcParameters
                List<Constraint> cons = constraintConfig.getForeignConstraints(tableName);

                // 对于每一个约束，判断是否违反
                for (Constraint con : cons) {
                    if (!con.getValidation()) continue; // 跳过无效约束
                    List<String> foreignKeys = con.getForeignKey();
                    List<String> primaryKeys = con.getPrimaryKey();


                    // 假如说约束是 foreign-key = {cc4, cc3} primary-key = {c4,c3}
                    // insert 语句是 insert into ft(cc1,cc2,cc3,cc4) values(1,?,2,?)
                    // 那么需要构建的 select count SQL 的条件判断将是 where c4 = ? and c3 = 2
                    // -----------------
                    // 因此解析 insert SQL
                    // foreignTableColumnNames = (cc1,cc2,cc3,cc4)
                    // foreignTableValues = (1,?,2,?)
                    // 这里注意第一个问号，由 parameterMappings[0] 负责，第二个问号，由 parameterMappings[1] 负责
                    // ------------------
                    // 我们用 countParameterMappings 封装 select count SQL 中问号对应的 ParameterMapping，
                    // 这个 mapping 直接来自上面的 parameterMappings
                    // 用 countColValMap 封装字面量，如本例中 c3->2
                    // -------------------
                    // 我们遍历 con.getForeignKey() {cc4, cc3}
                    // 第一轮，cc4，对应 foreignTableColIndex = cc4 in (cc1,cc2,cc3,cc4) = 3
                    // 查看 foreignTableColIndex = 3 对应的 values(1,?,2,?) ，是 问号
                    // 并且知道是第2个问号，所以 countParameterMappings.add(parameterMappings[1])
                    // -------------------
                    // 第二轮，cc3，对应 foreignTableColIndex = cc3 in (cc1,cc2,cc3,cc4) = 2
                    // 查看 foreignTableColIndex = 2 对应的 values(1,?,2,?) ，是 2 ，是一个字面量
                    // 所以在 countColValMap 中添加 c3->2
                    // 怎么找到 列名 c3 呢？ primaryKeys.get(i) ,因为约束 Constraint 中“主键”和“外键”一一对应
                    List<ParameterMapping> countParameterMappings = new ArrayList<>();
                    Map<String, String> countColValMap = new HashMap<>();
                    for (int i = 0; i < foreignKeys.size(); i++) {
                        String foreignKey = foreignKeys.get(i);

                        int foreignTableColIndex = foreignTableColumnNames.indexOf(foreignKey);
                        if (foreignTableColIndex == -1) {
                            throw new SQLParseException("columns in insert SQL(" + sql
                                    + ") cannot match constraint(" + con + "), maybe you want insert null value at "
                                    + tableName + "(" + foreignKey + ")?");
                        } else {
                            String foreignTableValue = foreignTableValues.get(foreignTableColIndex);
                            if (foreignTableValue.equals("?")) {
                                int numberOfQuestionMarkBefore =
                                        StringUtils.numberOfQuestionMarkBefore(foreignTableColIndex, foreignTableValues);
                                countParameterMappings.add(parameterMappings.get(numberOfQuestionMarkBefore));
                            } else {
                                countColValMap.put(primaryKeys.get(i), foreignTableValue);
                            }
                        }
                    }


//                    for (int i = 0; i < parameterMappings.size(); i++) {
//                        ParameterMapping pm = parameterMappings.get(i);
//                        String col = columns.get(i);
//                        Expression exp = expressions.get(i);
//                        if (foreignKeys.contains(col)) {
//                            if (exp instanceof JdbcParameter) {
//                                foreignKeysMappingArray[foreignKeys.indexOf(col)] = pm;
//                            } else {
//                                //ParameterMapping
//                            }
//                        }
//                    }

                    String sqlSelectFromPrimaryTable = sqlGenerator.selectCountFromPrimaryTablePrimaryKeys(con, countColValMap);

                    BoundSql countBoundSql = new BoundSql(
                            ms.getConfiguration(),
                            sqlSelectFromPrimaryTable,
                            countParameterMappings,
                            parameter
                    );

                    MappedStatement countMs = createMappedStatement(ms, Long.class, ignore -> countBoundSql);

                    CacheKey cacheKey = executor.createCacheKey(
                            countMs,
                            parameter,
                            RowBounds.DEFAULT,
                            countBoundSql
                    );

                    // 查询 count
                    Object countResultList = executor.query(
                            countMs,
                            parameter,
                            RowBounds.DEFAULT,
                            Executor.NO_RESULT_HANDLER, // 别人也是 null
                            cacheKey,
                            countBoundSql
                    );

                    Long count = (Long) ((List) countResultList).get(0);

                    // 违反约束
                    if (count <= 0) throw ForeignKeyConstraintException.insertToForeignTableFailed(con, parameter);
                }


                // 如果是的，判断插入中外键是否在主表中存在

                // 执行 sql SELECT COUNT(*) FROM 主表 WHERE pk = val LIMIT 1
                // 查看返回值数目


                break;
            case UPDATE:
                break;
            case DELETE:
                break;
            case REPLACE:
                break;
        }

        // proceed
        return invocation.proceed();
    }

    /**
     * 创建一个返回值为 Long 的 MappedStatement
     */
    private MappedStatement createMappedStatement(MappedStatement ms, Class<Long> resultType,
                                                  SqlSource sqlSource) {

        MappedStatement.Builder builder = new MappedStatement.Builder(
                ms.getConfiguration(),
                ms.getId() + "_insert_constraint",
                sqlSource, // 产生 bound sql
                SqlCommandType.SELECT
        );

        builder.resource(ms.getResource());

        builder.fetchSize(ms.getFetchSize());

        builder.statementType(StatementType.PREPARED);

        builder.keyGenerator(null);

        builder.keyProperty(null);

        builder.timeout(ms.getTimeout()); // default null

        ParameterMap.Builder parameterMapBuilder = new ParameterMap.Builder(
                ms.getConfiguration(),
                ms.getId() + "_insert_constraint_parameterMap",
                null,
                Collections.EMPTY_LIST // empty
        );
        builder.parameterMap(parameterMapBuilder.build());

        List<ResultMap> resultMaps = new ArrayList<>();
        ResultMap.Builder resultMapBuilder = new ResultMap.Builder(
                ms.getConfiguration(),
                ms.getId() + "_insert_constraint_resultMap",
                resultType,
                Collections.EMPTY_LIST // empty
        );
        resultMaps.add(resultMapBuilder.build());
        builder.resultMaps(resultMaps);

        builder.resultSetType(ResultSetType.DEFAULT);

        builder.cache(ms.getCache());

        builder.flushCacheRequired(false); // select no flush

        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    private final String DEFAULT_CONFIG_FILE = "constraints.yml";

    @Override
    public void setProperties(Properties properties) {
        // 如果通过 mybatis 主配置文件中，指定了 config 属性，直接加载
        if (((Hashtable) properties).containsKey("config")) {
            String config = properties.getProperty("config");
            constraintConfig = parse(config);
        }
        // 如果存在 constraints.yml 文件，直接加载
        else if (hasConfigFile(DEFAULT_CONFIG_FILE)) {
            constraintConfig = parse(DEFAULT_CONFIG_FILE);
        }


        // TODO 也可以通过 setConstraintConfig 外部注入 constraintConfig
    }

    private boolean hasConfigFile(String configFile) {
        InputStream resourceAsStream;

        try {
            resourceAsStream = Resources.getResourceAsStream(configFile);
        } catch (IOException e) {
            resourceAsStream = null;
        }

        return resourceAsStream != null;
    }

    /**
     * 解析 configFile 文件
     *
     * @param configFile 配置文件
     * @return constraintConfig
     */
    @SuppressWarnings("unchecked")
    private ConstraintConfig parse(String configFile) {
        ConstraintConfig constraintConfig = new ConstraintConfig();

        try (InputStream in = Resources.getResourceAsStream(configFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> map = yaml.loadAs(in, Map.class);

            List<Map<String, Object>> constraints = (List<Map<String, Object>>) map.get("constraints");

            for (Map<String, Object> con : constraints) {
                Constraint constraint = new Constraint();

                String name = (String) con.get("name");
                Boolean validation = (Boolean) con.get("validation");
                String primaryTable = (String) con.get("primary-table");
                String foreignTable = (String) con.get("foreign-table");
                String onDelete = (String) con.get("on-delete");
                String onUpdate = (String) con.get("on-update");

                if (name == null || name.isEmpty()) name = "fk_" + foreignTable + "_" + primaryTable;
                if (validation == null) validation = true; // 默认开启

                constraint.setName(name);
                constraint.setValidation(validation);
                constraint.setPrimaryTable(primaryTable);
                constraint.setPrimaryKey(Arrays.asList(((String) con.get("primary-key")).split(",")));
                constraint.setForeignTable(foreignTable);
                constraint.setForeignKey(Arrays.asList(((String) con.get("foreign-key")).split(",")));
                constraint.setOnDelete(ConstraintAction.resolve(onDelete));
                constraint.setOnUpdate(ConstraintAction.resolve(onUpdate));

                constraintConfig.addConstraint(constraint);
            }

            return constraintConfig;
        } catch (IOException e) {
            throw new ConfigException("Error parsing file [" + configFile + "]", e);
        }
    }


    public ConstraintConfig getConstraintConfig() {
        return constraintConfig;
    }

    public void setConstraintConfig(ConstraintConfig constraintConfig) {
        this.constraintConfig = constraintConfig;
    }
}
