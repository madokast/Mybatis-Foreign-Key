package com.github.foreignkey.test;


import com.github.foreignkey.Constraint;
import com.github.foreignkey.ConstraintAction;
import com.github.foreignkey.ConstraintConfig;
import org.apache.ibatis.io.Resources;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Description
 * 测试 yml 文件解析
 * <pre>
 * constraints:
 *   - primary-table: tb_user
 *     primary-key: id
 *     foreign-table: tb_user_role
 *     foreign-key: user_id
 *     on-update: RESTRICT
 *     on-delete: RESTRICT
 *   - primary-table: tb_role
 *     primary-key: id
 *     foreign-table: tb_user_role
 *     foreign-key: role_id
 * <pre/>
 * <p>
 *
 * @author madokast
 * @version 1.0
 */

public class TestYmlParse {

    @Test
    public void validate() throws IOException {
        ConstraintConfig constraintConfig = new ConstraintConfig();

        try (InputStream in = Resources.getResourceAsStream("constraints.yml");) {
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

                constraintConfig.addConstraint(constraint);
            }
        }
    }
}
