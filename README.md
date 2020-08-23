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
### (2/2) 编写配置文件 constraints.yml
配置文件采用 yml 格式，目的是为了容易书写 list 形式的配置，格式如下
~~~
constraints:
  - name: 约束名（可选）
    validation: true/false 是否有效（可选）
    primary-table: 被参照的表
    primary-key: 被参照的属性名/列名
    foreign-table: 外键所在表
    foreign-key: 外键
    on-update: ON UPDATE 行为（可选）
    on-delete: ON DELETE 行为（可选）
  - 定义更多外键约束
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
利用本插件，可以将**物理外键**转为**应用层外键**，配置文件书写如下（数据库中不再需要添加物理外键）
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
5. 配置文件使用默认名称 constraints.yml 并放在根目录时，本插件能自动读取。否则应在插件配置中，指定配置文件的位置 
~~~
<plugins>
    <plugin interceptor="com.github.foreignkey.ForeignKeyInterceptor">
        <property name="config" value="database/foreign-keys.yml"/>
    </plugin>
</plugins>
~~~

## 难点一 SQL语句中字面量和参数(?)的解析
假如说约束是 foreign-key = {cc4, cc3} 和 primary-key = {c4,c3}

拦截到 insert 语句 SQL1

~~~
# SQL1
INSERT INTO foreign-table(cc1,cc2,cc3,cc4) VALUES(1,?,2,?)
~~~

那么需要构建 SQL2 查询主表是否存在记录

~~~
# SQL2
SELECT COUNT FROM primary-table WHERE c4 = ? AND c3 = 2 LIMIT 1
~~~


首先解析 SQL1
~~~
cols = [cc1, cc2, cc3, cc4]
vals = [1, ?, 2, ?]
~~~

在 Mybatis 中使用 List<ParameterMapping> 配置 SQL 中每个 ? 的参数，第一个问号，由 parameterMappings[0] 负责，第二个问号，由 parameterMappings[1] 负责等。

因此我们同样用一个 list 封装 SQL2 中问号对应的 ParameterMapping，
这个 ParameterMapping 直接来自上面的 List<ParameterMapping>，但是注意顺序有可能发生变化，且不是所有的 parameterMappings 都放入这个 list 中。

另外，使用一个 map 封装字面量，如本例中 c3->2。

我们遍历 foreign-key [cc4, cc3]。

第一轮，foreign-key[0] = cc4，对应 colIndex = cc4 in [cc1, cc2, cc3, cc4] = 3

查看 colIndex = 3 对应的 vals[1,?,2,?]，是问号

并且可以知道是第2个问号，所以 list.add(parameterMappings[1])

第二轮，foreign-key[1] = cc3，对应 colIndex = cc3 in [cc1,cc2,cc3,cc4] = 2

查看 colIndex = 2 对应的 vals[1,?,2,?]，是一个字面量 2

所以在 map 中添加 c3->2

怎么找到列名 c3 呢？由 primary-key[1] 得到 c3，因为配置的约束 Constraint 中“主键”和“外键”是一一对应的。

至此 SQL2 中所需数据都封装到了 list 和 map 中。