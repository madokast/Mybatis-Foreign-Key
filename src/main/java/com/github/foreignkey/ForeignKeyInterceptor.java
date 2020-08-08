package com.github.foreignkey;


import com.github.foreignkey.exception.ConfigException;
import com.github.foreignkey.exception.ForeignKeyConstraintException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
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

    private ConstraintConfig constraintConfig;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Executor executor = (Executor) invocation.getTarget();


        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];

        BoundSql boundSql = ms.getBoundSql(parameter);

        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        String sql = boundSql.getSql();


        switch (UpdateType.resolve(sql)) {
            case INSERT:
                // 判断插入是不是在从表中进行
                Insert insert = (Insert) CCJSqlParserUtil.parse(sql);
                String table = StringUtils.dropUnquote(insert.getTable().getName());
                List<String> columns = insert.getColumns().stream().map(Column::getColumnName).map(StringUtils::dropUnquote).collect(Collectors.toList());
                List<Expression> expressions = ((ExpressionList) insert.getItemsList()).getExpressions();// JdbcParameters
                List<Constraint> cons = constraintConfig.getForeignConstraints(table);
                // 对于每一个约束，判断是否违反
                for (Constraint con : cons) {
                    if (!con.getValidation()) continue;
                    List<String> foreignKeys = con.getForeignKey();

                    ParameterMapping[] psArr = new ParameterMapping[foreignKeys.size()];
                    for (int i = 0; i < parameterMappings.size(); i++) {
                        ParameterMapping pm = parameterMappings.get(i);
                        String col = columns.get(i);
                        Expression exp = expressions.get(i);
                        if (exp instanceof JdbcParameter && foreignKeys.contains(col)) {
                            psArr[foreignKeys.indexOf(col)] = pm;
                        }
                    }

                    String sqlSelectFromPrimaryTable = con.getSqlSelectFromPrimaryTableByPrimaryKeys();

                    BoundSql countBoundSql = new BoundSql(
                            ms.getConfiguration(),
                            sqlSelectFromPrimaryTable,
                            Arrays.asList(psArr),
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
