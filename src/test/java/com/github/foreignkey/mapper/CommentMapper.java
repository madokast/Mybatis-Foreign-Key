package com.github.foreignkey.mapper;


import com.github.foreignkey.entity.Comment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description
 * 用户评论表 mapper
 * <p>
 *
 * @author madokast
 * @version 1.0
 */

public interface CommentMapper {

    Comment selectById(Integer id);

    List<Comment> selectByUserId(String userId);

    void insert(Comment comment);

    void insertParam(String userId,String content);

    void insertParamReverse(String userId,String content);

    void insertArg(String userId,String content);

    void insertParamName(@Param("userId") String userId,@Param("content") String content);

    void insertStaticComment1();

    void insertStaticComment2();

    Long count(String userId);
}
