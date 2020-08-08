package com.github.foreignkey.entity;


/**
 * 用户发言表
 * `id` int(11) NOT NULL AUTO_INCREMENT,
 * `user_id` varchar(40) NOT NULL,
 * `content` varchar(255) NOT NULL,
 *
 * @author madokast
 * @version 1.0
 */

public class Comment {

    private Integer id;

    private String userId;

    private String content;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
