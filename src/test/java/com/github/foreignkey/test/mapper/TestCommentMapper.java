package com.github.foreignkey.test.mapper;

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

    @Test(expected = ForeignKeyConstraintException.class)
    public void insertFailTest() throws Throwable {
        try {
            SqlSession sqlSession = sqlSessionFactory.openSession(true);

            CommentMapper mapper = sqlSession.getMapper(CommentMapper.class);

            Comment comment = new Comment();
            comment.setContent("test comment");
            comment.setUserId("a05");
            mapper.insert(comment);
            LOGGER.debug(comment.toString());
            Assert.assertEquals((Object) 4, comment.getId());
        } catch (PersistenceException e) {
            Throwable cause = e.getCause();
            LOGGER.debug(cause.getMessage());
            throw cause;
        }
    }

    @Test
    public void insertParamTest() {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);

        CommentMapper mapper = sqlSession.getMapper(CommentMapper.class);

        mapper.insertParam("a01", "test comment");


        Comment comment = new Comment();
        comment.setId(4);
        comment.setContent("test comment");
        comment.setUserId("a01");


        Comment comment4 = mapper.selectById(4);

        Assert.assertEquals(comment4, comment);
    }

    @Test(expected = ForeignKeyConstraintException.class)
    public void insertParamFailedTest() throws Throwable {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);

        CommentMapper mapper = sqlSession.getMapper(CommentMapper.class);

        try {
            mapper.insertParam("XXX", "test comment");
        } catch (PersistenceException e) {
            Throwable cause = e.getCause();
            LOGGER.debug(cause.getMessage());
            throw cause;
        }
    }

    @Test
    public void insertParamReverseTest() {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);

        CommentMapper mapper = sqlSession.getMapper(CommentMapper.class);

        mapper.insertParamReverse("a01", "test comment");


        Comment comment = new Comment();
        comment.setId(4);
        comment.setContent("test comment");
        comment.setUserId("a01");


        Comment comment4 = mapper.selectById(4);

        Assert.assertEquals(comment4, comment);
    }

    @Test(expected = ForeignKeyConstraintException.class)
    public void insertParamReverseFailedTest() throws Throwable {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);

        CommentMapper mapper = sqlSession.getMapper(CommentMapper.class);

        try {
            mapper.insertParamReverse("XXX", "test comment");
        } catch (PersistenceException e) {
            Throwable cause = e.getCause();
            LOGGER.debug(cause.getMessage());
            throw cause;
        }
    }

    @Test
    public void insertArgTest() {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);

        CommentMapper mapper = sqlSession.getMapper(CommentMapper.class);

        mapper.insertArg("a01", "test comment");


        Comment comment = new Comment();
        comment.setId(4);
        comment.setContent("test comment");
        comment.setUserId("a01");


        Comment comment4 = mapper.selectById(4);

        Assert.assertEquals(comment4, comment);
    }

    @Test(expected = ForeignKeyConstraintException.class)
    public void insertArgFailedTest() throws Throwable {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);

        CommentMapper mapper = sqlSession.getMapper(CommentMapper.class);

        try {
            mapper.insertArg("XXX", "test comment");
        } catch (PersistenceException e) {
            Throwable cause = e.getCause();
            LOGGER.debug(cause.getMessage());
            throw cause;
        }
    }

    @Test
    public void insertStaticComment1Test() {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);

        CommentMapper mapper = sqlSession.getMapper(CommentMapper.class);

        //    <insert id="insertStaticComment1">
        //        INSERT INTO `tb_comment`(`user_id`, `content`)
        //        VALUES ('r01', '这本小说真有趣')
        //    </insert>
        mapper.insertStaticComment1();

        Comment comment = new Comment();
        comment.setId(4);
        comment.setUserId("r01");
        comment.setContent("这本小说真有趣");

        Comment comment4 = mapper.selectById(4);

        Assert.assertEquals(comment4, comment);
    }

    @Test(expected = ForeignKeyConstraintException.class)
    public void insertStaticComment2Test() throws Throwable {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);

        CommentMapper mapper = sqlSession.getMapper(CommentMapper.class);


        //    <insert id="insertStaticComment2">
        //        INSERT INTO `tb_comment`(`user_id`, `content`)
        //        VALUES ('s01', '我是谁我在哪')
        //    </insert>
        try {
            mapper.insertStaticComment2();
        } catch (PersistenceException e) {
            Throwable cause = e.getCause();
            LOGGER.debug(cause.getMessage());
            throw cause;
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
