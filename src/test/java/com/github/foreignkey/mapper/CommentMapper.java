package com.github.foreignkey.mapper;


import com.github.foreignkey.entity.Comment;

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

    List<Comment> selectByUserId(String userId);

    void insert(Comment comment);

    Long count(String userId);
}
