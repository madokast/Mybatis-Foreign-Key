package com.github.foreignkey.mapper;

import com.github.foreignkey.entity.User;

/**
 * Description
 * 用户表 mapper
 *
 * @author madokast
 * @version 1.0
 */

public interface UserMapper {

    User selectById(String id);

    void insert(User user);

    void updateById(User user);

    void deleteById(User user);
}
