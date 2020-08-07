package com.github.foreignkey.test;

import com.github.foreignkey.entity.User;
import com.github.foreignkey.mapper.UserMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

/**
 * @author madokast
 * @version 1.0
 */

public class TestForeignKeyInterceptor {
    private final Log LOGGER = LogFactory.getLog(TestForeignKeyInterceptor.class);

    SqlSessionFactory sqlSessionFactory;

    /**
     * 获取 sqlSessionFactory
     * 初始化测试数据
     */
    @Before
    public void init() throws IOException {
        InputStream config = Resources.getResourceAsStream("mybatis-config.xml");

        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);

        Connection connection = sqlSessionFactory.openSession().getConnection();

        ScriptRunner scriptRunner = new ScriptRunner(connection);

        scriptRunner.runScript(Resources.getResourceAsReader("test.sql"));
    }

    @Test
    public void insertTest() {
        String userId = "r03";
        String username = "reader3";

        try (SqlSession sqlSession = sqlSessionFactory.openSession(true);) {
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);

            User user1 = mapper.selectById(userId);
            Assert.assertNull(user1);

            User user2 = new User();
            user2.setId(userId);
            user2.setUsername(username);
            mapper.insert(user2);

            sqlSession.clearCache();

            User user3 = mapper.selectById(userId);
            Assert.assertEquals(user3.getId(), userId);
            Assert.assertEquals(user3.getUsername(), username);
        }
    }


}
