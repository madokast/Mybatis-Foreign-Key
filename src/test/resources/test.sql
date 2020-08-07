DROP TABLE IF EXISTS `tb_user_role`;
DROP TABLE IF EXISTS `tb_user`;
DROP TABLE IF EXISTS `tb_role`;


CREATE TABLE `tb_user`
(
    `id`       varchar(40) NOT NULL,
    `username` varchar(20) NOT NULL,
    PRIMARY KEY (`id`)
) comment '用户表';

CREATE TABLE `tb_role`
(
    `id`          int(11)     NOT NULL AUTO_INCREMENT,
    `name`        varchar(20) NOT NULL,
    `description` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) comment '角色表';

CREATE TABLE `tb_user_role`
(
    `id`      int(11)     NOT NULL AUTO_INCREMENT,
    `user_id` varchar(40) NOT NULL,
    `role_id` int(11)     NOT NULL,
    PRIMARY KEY (`id`)
    -- CONSTRAINT `fk_user_role_t_role_1` FOREIGN KEY (`role_id`) REFERENCES `tb_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    -- CONSTRAINT `fk_user_role_t_user_1` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) comment '用户角色表';

INSERT INTO `tb_role`(`id`, `name`)
VALUES ('1', '读者'),
       ('2', '作者'),
       ('3', '管理员');

INSERT INTO `tb_user`(`id`, `username`)
VALUES ('r01', 'reader1'),
       ('r02', 'reader2'),
       ('w01', 'author1'),
       ('w02', 'author2'),
       ('a01', 'admin1'),
       ('a02', 'admin2');

INSERT INTO `tb_user_role`(`user_id`, `role_id`)
VALUES ('r01', '1'),
       ('r02', '1'),
       ('w01', '2'),
       ('w02', '2'),
       ('a01', '3'),
       ('a02', '3');


--  外键语法
--  [CONSTRAINT symbol] FOREIGN KEY [id] (index_col_name, ...)
--  REFERENCES tbl_name (index_col_name, ...)
--  [ON DELETE {RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT}]
--  [ON UPDATE {RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT}]
--  在父表上进行update/delete以更新或删除在子表中有一条或多条对应匹配行的候选键时，
--  父表的行为取决于：在定义子表的外键时指定的on update/on delete子句。
--  ① RESTRICT（限制外表中的外键改动，默认值）
--  ② CASCADE（跟随外键改动）
--  ③ SET NULL（设空值）
--  ④ SET DEFAULT（设默认值）
--  ⑤ NO ACTION（无动作，默认的）

