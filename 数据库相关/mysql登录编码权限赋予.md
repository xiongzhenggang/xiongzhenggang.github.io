## mysql使用指南
在windows下：
<p>
数据库安装在C:DOS命令窗口中输入 net start mysql（安装版）关闭：net stop mysql
  非安装版：直接在bin中找到mysqld.exe双击或者 关闭：mysqladmin -uroot -p shutdown

在D：cd D:\Tools\MySQL5.5.25\bin进入到mysql的bin目录下才可以输入 mysql -hlocalhost -uroot -p

 java链接// MySQL的JDBC URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值
</P>
* 修改管理员密码
```xml
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '***'

查看端口号

mysql> show global variables like 'port';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| port          | 3307  |
+---------------+-------+
1 row in set, 1 warning (0.12 sec)
```
### 用户权限管理设置
```xml
创建
mysql> create user zx_root IDENTIFIED by '12345';   //identified by 会将纯文本密码加密作为散列值存储

查看其权限
mysql>show grants for zx_root;

修改
mysql>rename   user  feng  to   newuser；//mysql 5之后可以使用，之前需要使用update 更新user表
删除
mysql>drop user newuser;   //mysql5之前删除用户时必须先使用revoke 删除用户权限，然后删除用户，mysql5之后drop 命令可以删除用户的同时删除用户的相关权限
更改密码
mysql> set password for zx_root =password('xxxxxx');
 mysql> update  mysql.user  set  password=password('xxxx')  where user='otheruser'
查看用户权限
mysql> show grants for zx_root;
赋予权限
mysql> grant select on dmc_db.*  to zx_root;
回收权限
mysql> revoke  select on dmc_db.*  from  zx_root;  //如果权限不存在会报错
 
上面的命令也可使用多个权限同时赋予和回收，权限之间使用逗号分隔
mysql> grant select，update，delete  ，insert  on dmc_db.*  to  zx_root;
如果想立即看到结果使用
flush  privileges ;
命令更新 
设置权限时必须给出一下信息
1，要授予的权限
2，被授予访问权限的数据库或表
3，用户名
grant和revoke可以在几个层次上控制访问权限
1，整个服务器，使用 grant ALL  和revoke  ALL
2，整个数据库，使用on  database.*
3，特点表，使用on  database.table
4，特定的列
5，特定的存储过程
 
user表中host列的值的意义
%              匹配所有主机
localhost    localhost不会被解析成IP地址，直接通过UNIXsocket连接
127.0.0.1      会通过TCP/IP协议连接，并且只能在本机访问；
::1                 ::1就是兼容支持ipv6的，表示同ipv4的127.0.0.1

```
### 以下一些命令操作

grant 普通数据用户，查询、插入、更新、删除 数据库中所有表数据的权利。

grant select on testdb.* to common_user@’%’

grant insert on testdb.* to common_user@’%’

grant update on testdb.* to common_user@’%’

grant delete on testdb.* to common_user@’%’

或者，用一条 MySQL 命令来替代：

grant select, insert, update, delete on testdb.* to common_user@’%’

9>.grant 数据库开发人员，创建表、索引、视图、存储过程、函数。。。等权限。

grant 创建、修改、删除 MySQL 数据表结构权限。

grant create on testdb.* to developer@’192.168.0.%’;

grant alter on testdb.* to developer@’192.168.0.%’;

grant drop on testdb.* to developer@’192.168.0.%’;

grant 操作 MySQL 外键权限。

grant references on testdb.* to developer@’192.168.0.%’;

grant 操作 MySQL 临时表权限。

grant create temporary tables on testdb.* to developer@’192.168.0.%’;

grant 操作 MySQL 索引权限。

grant index on testdb.* to developer@’192.168.0.%’;

grant 操作 MySQL 视图、查看视图源代码 权限。

grant create view on testdb.* to developer@’192.168.0.%’;

grant show view on testdb.* to developer@’192.168.0.%’;

grant 操作 MySQL 存储过程、函数 权限。

grant create routine on testdb.* to developer@’192.168.0.%’; -- now, can show procedure status

grant alter routine on testdb.* to developer@’192.168.0.%’; -- now, you can drop a procedure

grant execute on testdb.* to developer@’192.168.0.%’;

10>.grant 普通 DBA 管理某个 MySQL 数据库的权限。

grant all privileges on testdb to dba@’localhost’

其中，关键字 “privileges” 可以省略。

11>.grant 高级 DBA 管理 MySQL 中所有数据库的权限。

grant all on *.* to dba@’localhost’

### 权限划分

12>.MySQL grant 权限，分别可以作用在多个层次上。

1. grant 作用在整个 MySQL 服务器上：

grant select on *.* to dba@localhost; -- dba 可以查询 MySQL 中所有数据库中的表。

grant all on *.* to dba@localhost; -- dba 可以管理 MySQL 中的所有数据库

2. grant 作用在单个数据库上：

grant select on testdb.* to dba@localhost; -- dba 可以查询 testdb 中的表。

3. grant 作用在单个数据表上：

grant select, insert, update, delete on testdb.orders to dba@localhost;

4. grant 作用在表中的列上：

grant select(id, se, rank) on testdb.apache_log to dba@localhost;

5. grant 作用在存储过程、函数上：

grant execute on procedure testdb.pr_add to ’dba’@’localhost’

grant execute on function testdb.fn_add to ’dba’@’localhost’

* 注意：修改完权限以后 一定要刷新服务，或者重启服务，刷新服务用：FLUSH PRIVILEGES。

### 添加字段
```xml
mysql> alter table abc add timetest datetime;
Query OK, 2 rows affected (0.73 sec)
Records: 2  Duplicates: 0  Warnings: 0
MySQL：MySQL日期数据类型、MySQL时间类型使用总结 MySQL 日期类型：日期格式、所占存储空间、日期范围 比较。 
日期类型        存储空间      日期格式                日期范围
------------  ---------  --------------------- -----------------------------------------
datetime      8 bytes  YYYY-MM-DD HH:MM:SS  1000-01-01 00:00:00 ~ 9999-12-31 23:59:59
timestamp      4 bytes  YYYY-MM-DD HH:MM:SS  1970-01-01 00:00:01 ~ 2038
date          3 bytes  YYYY-MM-DD            1000-01-01          ~ 9999-12-31
year          1 bytes  YYYY                  1901                ~ 2155

在 MySQL 中创建表时，对照上面的表格，很容易就能选择到合适自己的数据类型。不过到底是选择 datetime 还是 timestamp，可能会有点犯难。这两个日期时间类型各有优点：datetime 的日期范围比较大；timestamp 所占存储空间比较小，只是 datetime 的一半。 

另外，timestamp 类型的列还有个特性：默认情况下，在 insert, update 数据时，timestamp 列会自动以当前时间（CURRENT_TIMESTAMP）填充/更新。“自动”的意思就是，你不去管它，MySQL 会替你去处理。 
```
### 一般情况下，我倾向于使用 datetime 日期类型。 
* 修改某一字段值
```
mysql> update abc set timetest='2015-12-11 11:25:45' where a=2;
Query OK, 2 rows affected (0.00 sec)
Rows matched: 2  Changed: 2  Warnings: 0
mysql> select * from abc;
+---+----+---+---+---------------------+
| a | b  | c | d | timetest            |
+---+----+---+---+---------------------+
| 2 |  3 | 4 | 9 | 2015-12-11 11:25:45 |
| 2 | 34 | 4 | 4 | 2015-12-11 11:25:45 |
+---+----+---+---+---------------------+
2 rows in set (0.00 sec)

1.增加字段：
    alter table   tablename    add   new_field_id   type   not null default '0'; 
    例： 
    alter table mmanapp_mmanmedia add appid_id integer not null default 372;
    增加主键：
    alter table  tabelname   add   new_field_id   type    default 0 not null auto_increment ,add   primary key (new_field_id); 
    增加外键：
    在已经存在的字段上增加外键约束
    ALTER TABLE yourtablename    ADD [CONSTRAINT symbol] FOREIGN KEY [id] (index_col_name, ...)    REFERENCES tbl_name (index_col_name, ...)    [ON DELETE {RESTRICT | CASCADE | SET NULL | NO ACTION}]    [ON UPDATE {RESTRICT | CASCADE | SET NULL | NO ACTION}] 
 
2.删除字段:
    alter table    tablename    drop column      colname; 
     例： 
     alter table   mmanapp_mmanmedia    drop column     appid_id;

3.修改字段名：
     alter table     tablename    change   old_field_name    new_field_name  old_type;

4.修改字段类型：
    alter table     tablename     change    filed_name   filed_name   new_type;  

修改类型可以用（谨慎修改类型，可能会导致原有数据出错）
MySQL> alter table address modify column city char(30);

建表设置主键自增

CREATE TABLE EM_VOTE_INFO
(
    F_ID                       INT NOT NULL AUTO_INCREMENT,
    F_VOTE_TITLE                	 VARCHAR(255),
    F_VOTE_START			 TIMESTAMP NOT NULL,
    F_VOTE_END           		 TIMESTAMP NOT NULL,
   F_VOTE_ITEM_TYPE       		TINYINT,
    F_VOTE_IS_DISPLAY                    TINYINT,
    PRIMARY KEY(F_ID) 
);
```
### mysql编码问题
```
字符问题出现？？
mysql> select * from role;
+--------+----------+----------+---------+-----------+
| roleId | roleInfo | roleName | proleId | proleName |
+--------+----------+----------+---------+-----------+
| T_EMP  | NULL     | ??????   | NULL    | NULL      |
+--------+----------+----------+---------+-----------+
show variables like 'character%';查看字符编码
--更改字符集  
SET character_set_client = utf8 ;  
SET character_set_connection = utf8 ;  
SET character_set_database = utf8 ;  
SET character_set_results = utf8 ;  
SET character_set_server = utf8 ;  
SET collation_connection = utf8 ;  
SET collation_database = utf8 ;  
SET collation_server = utf8 ;  
连接数据库时：
jdbc:mysql://localhost:3306/数据库名?useUnicode=true&amp;characterEncoding=utf8
查询之前：
set names gbk;
存中文显示长度太长,但是字段长度够长,说明字符集没设置
C:\Program Files\MySQL\MySQL Server 5.0\my.ini\
default-character-set=utf8
default-character-set=utf8
```
2. 更改表结构与数据库编码统一
```sql
CREATE TABLE role 
(
    roleId                        	VARCHAR(20) NOT NULL,
    roleInfo               	 		VARCHAR(50),
    roleName			  			VARCHAR(50),
    proleId				 		 	VARCHAR(20),
    proleName			 			VARCHAR(50), 
    PRIMARY KEY(roleId) 
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
```
```sql
mysql> INSERT INTO role(roleId, roleName) VALUES ('T_MAN', '经理');
Query OK, 1 row affected (0.06 sec)

mysql> select * from role;
+--------+----------+----------+---------+-----------+
| roleId | roleInfo | roleName | proleId | proleName |
+--------+----------+----------+---------+-----------+
| T_MAN  | NULL     | 经理     | NULL    | NULL      |
+--------+----------+----------+---------+-----------+
```
