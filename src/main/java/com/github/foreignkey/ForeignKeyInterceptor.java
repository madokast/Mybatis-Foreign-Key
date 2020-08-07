package com.github.foreignkey;


import com.github.foreignkey.exception.ConfigException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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

    private String configFile = "constraints.yml";

    private ConstraintConfig constraintConfig;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];

        BoundSql boundSql = ms.getBoundSql(parameter);

        String sql = boundSql.getSql();

        switch (UpdateType.resolve(sql)) {
            case INSERT:
                Insert insert = (Insert) CCJSqlParserUtil.parse(sql);
                String table = StringUtils.dropUnquote(insert.getTable().getName());
                List<String> columns = insert.getColumns().stream().map(Column::getColumnName).map(StringUtils::dropUnquote).collect(Collectors.toList());


                break;
            case UPDATE:
                break;
            case DELETE:
                break;
            case REPLACE:
                break;
        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        String config = properties.getProperty("config");
        if (config != null) configFile = config;
        constraintConfig = parse(configFile);
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
                constraint.setPrimaryTable((String) con.get("primary-table"));
                constraint.setPrimaryKey(Arrays.asList(((String) con.get("primary-key")).split(",")));
                constraint.setForeignTable((String) con.get("foreign-table"));
                constraint.setForeignKey(Arrays.asList(((String) con.get("foreign-key")).split(",")));
                constraint.setOnDelete(ConstraintAction.resolve((String) con.get("on-delete")));
                constraint.setOnUpdate(ConstraintAction.resolve((String) con.get("on-update")));

                validate(constraint);

                constraintConfig.addConstraint(constraint);
            }

            return constraintConfig;
        } catch (IOException e) {
            throw new ConfigException("Error parsing file [" + configFile + "]", e);
        }
    }

    /**
     * 检查 constraint 合法性
     *
     * @param constraint 一个外键约束
     */
    private void validate(Constraint constraint) {
        // TODO
    }

    public ConstraintConfig getConstraintConfig() {
        return constraintConfig;
    }

    public void setConstraintConfig(ConstraintConfig constraintConfig) {
        this.constraintConfig = constraintConfig;
    }
}
