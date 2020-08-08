# Mybatis-Foreign-Key
应用层实现外键约束的 Mybatis 插件

## 使用方法
### (1/2) 引入插件
在 Mybatis 主配置 xml 文件中，引入插件
~~~
<plugins>
    <plugin interceptor="com.github.foreignkey.ForeignKeyInterceptor"/>
</plugins>
~~~
同时在项目中放入配置文件 constraints.yml (例如 maven 项目即放在 resources 文件夹中)
也可以利用 property 标签，指定配置文件的地址和名字
~~~
<plugins>
    <plugin interceptor="com.github.foreignkey.ForeignKeyInterceptor">
        <property name="config" value="database/foreign-keys.yml"/>
    </plugin>
</plugins>
~~~
### (2/2) 编写配置文件
配置文件采用 yml 格式，目的是为了容易书写 list 形式的配置，格式如下
~~~
constraints:
  - name: 约束名
    validation: true/false 是否有效
    primary-table: 被参照的表
    primary-key: 被参照的属性名/列名
    foreign-table: 外键所在表
    foreign-key: 外键
    on-update: ON UPDATE 行为
    on-delete: ON DELETE 行为
  - 定义更多外键
~~~
例如我们有用户表 tb_user(id, name) 和用户发言表 tb_comment(id,user_id,content)，很明显 tb_comment.user_id 是 tb_user.id 的外键

在数据库中实现**物理外键**，需要运行如下的 SQL 语句
~~~
ALTER TABLE `tb_comment`
    ADD CONSTRAINT `fk_comment_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `tb_user` (`id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT;
~~~
利用本插件，可以将**物理外键**转为**应用层外键**，配置文件书写如下（数据库中不再需要添加**物理外键**）
~~~
constraints:
  - name: fk_comment_user
    primary-table: tb_user
    primary-key: id
    foreign-table: tb_comment
    foreign-key: user_id
    on-update: NO-ACTION
    on-delete: CASCADE
~~~
配置文件细节
1. name 属性可选。默认值为 fk_外表名_主表名
2. validation 属性可选。表示该外键约束是否有效，默认 true
3. 当 primary-key 和 foreign-key 是复合属性时，用英文逗号隔开，例如: col1, col2
4. on-update 和 on-delete 属性可选。它们即数据库外键约束中的 ON UPDATE 和 ON DELETE ，
属性值可以是 RESTRICT | CASCADE | SET-NULL | NO-ACTION | SET-DEFAULT。
大小写不敏感。
默认均为 NO-ACTION。
各个值的含义可参考任意一本关系数据库书籍。