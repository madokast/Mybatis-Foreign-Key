package com.github.foreignkey.test;

import com.github.foreignkey.entity.Comment;
import com.github.foreignkey.exception.ForeignKeyConstraintException;
import com.github.foreignkey.mapper.CommentMapper;
import org.apache.ibatis.exceptions.PersistenceException;
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
import java.util.List;


/**
 * Description
 * 测试 CommentMapper
 * <p>
 *
 * @author madokast
 * @version 1.0
 */

public class TestCommentMapper {
    private final Log LOGGER = LogFactory.getLog(TestCommentMapper.class);

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
    public void selectByUserIdTest() {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);

        CommentMapper mapper = sqlSession.getMapper(CommentMapper.class);

        List<Comment> comments = mapper.selectByUserId("r01");

        Assert.assertEquals(2, comments.size());

        System.out.println("comments = " + comments);
    }

    @Test
    public void insertTest() {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);

        CommentMapper mapper = sqlSession.getMapper(CommentMapper.class);

        Comment comment = new Comment();
        comment.setContent("test comment");
        comment.setUserId("a01");
        mapper.insert(comment);
        LOGGER.debug(comment.toString());
        Assert.assertEquals((Object) 4, comment.getId());
    }

    @Test
    public void insertFailTest() {
        try {
            SqlSession sqlSession = sqlSessionFactory.openSession(true);

            CommentMapper mapper = sqlSession.getMapper(CommentMapper.class);

            Comment comment = new Comment();
            comment.setContent("test comment");
            comment.setUserId("a05");
            mapper.insert(comment);
            LOGGER.debug(comment.toString());
            Assert.assertEquals((Object) 4, comment.getId());
        }catch (PersistenceException e){
            Throwable cause = e.getCause();
            LOGGER.debug(cause.getMessage());
            Assert.assertTrue(cause instanceof ForeignKeyConstraintException);
        }
    }

    @Test
    public void countTest() {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);

        CommentMapper mapper = sqlSession.getMapper(CommentMapper.class);

        long count = mapper.count("r01");

        Assert.assertEquals(2L, count);
    }
}
