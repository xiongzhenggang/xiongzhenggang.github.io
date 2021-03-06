
## mysql索引使用

1. 创建索引
* 单列
```sql
mysql> create index user_id on user(userId);
```
<p>如果是CHAR，VARCHAR类型，length可以小于字段实际长度；如果是BLOB和TEXT类型，必须指定 length
CREATE INDEX indexName ON mytable(username(length));
</p>
2. 组合索引
```sql
CREATE TABLE mytable(   
	ID INT NOT NULL, 
	username VARCHAR(16) NOT NULL, 
	city VARCHAR(50) NOT NULL, 
	age INT NOT NULL  
);
ALTER TABLE mytable ADD INDEX name_city_age (name(10),city,age);
```
<p>建表时，usernname长度为 16，这里用 10。这是因为一般情况下名字的长度不会超过10，这样会加速索引查询速度，还会减少索引文件的大小，提高INSERT的更新速度。
如果分别在 usernname，city，age上建立单列索引，让该表有3个单列索引，查询时和上述的组合索引效率也会大不一样，远远低于我们的组合索引。
虽然此时有了三个索引，但MySQL只能用到其中的那个它认为似乎是最有效率的单列索引。MySQL组合索引“最左前缀”的结果。简单的理解就是只从最左面的开始组合。
并不是只要包含这三列的查询都会用到该组合索引
</p>
3. 唯一索引几种创建方式：
```sql
CREATE UNIQUE INDEX indexName ON mytable(username(length)) 
```
###  修改表结构
```sql
ALTER mytable ADD UNIQUE [indexName] ON (username(length)) 
```
创建表的时候直接指定
```sql
CREATE TABLE mytable(   ID INT NOT NULL,    username VARCHAR(16) NOT NULL,   UNIQUE [indexName] (username(length))   );
```
4. 主键索引
它是一种特殊的唯一索引，不允许有空值。一般是在建表的时候同时创建主键索引：
```sql
CREATE TABLE mytable(   ID INT NOT NULL,    username VARCHAR(16) NOT NULL,   PRIMARY KEY(ID)   );
```
5. 使用索引的注意事项
使用索引时，有以下一些技巧和注意事项：

1.索引不会包含有NULL值的列
只要列中包含有NULL值都将不会被包含在索引中，复合索引中只要有一列含有NULL值，那么这一列对于此复合索引就是无效的。所以我们在数据库设计时不要让字段的默认值为NULL。

2.使用短索引
对串列进行索引，如果可能应该指定一个前缀长度。例如，如果有一个CHAR(255)的列，如果在前10个或20个字符内，多数值是惟一的，那么就不要对整个列进行索引。短索引不仅可以提高查询速度而且可以节省磁盘空间和I/O操作。

3.索引列排序
MySQL查询只使用一个索引，因此如果where子句中已经使用了索引的话，那么order by中的列是不会使用索引的。因此数据库默认排序可以符合要求的情况下不要使用排序操作；尽量不要包含多个列的排序，如果需要最好给这些列创建复合索引。

4.like语句操作
一般情况下不鼓励使用like操作，如果非使用不可，如何使用也是一个问题。like “%aaa%” 不会使用索引而like “aaa%”可以使用索引。

5.不要在列上进行运算
select * from users where YEAR(adddate)<2007;
将在每个行上进行运算，这将导致索引失效而进行全表扫描，因此我们可以改成:
select * from users where adddate<‘2007-01-01';

6.不使用NOT IN和<>操作

### 三 mysql触发器使用
1. 在mysql的命令提示符默认是以‘；’结尾，而在触发器中有‘；’会导致创建触发器的过程失败。解决方式

使用delimiter来决定以什么方式结尾。
```sql
mysql> DROP TRIGGER IF EXISTS tri_deltb;
Query OK, 0 rows affected (0.00 sec)

mysql> delimiter //
mysql> CREATE TRIGGER tri_deltb
    -> AFTER DELETE ON ta              ---------------------------AFTER、BEFORE触发器发生执行前后顺序。DELETE、INSERT等触发事件
    -> FOR EACH ROW
    -> BEGIN
    ->       delete from tb where id=Old.id;
    -> END//
Query OK, 0 rows affected (0.06 sec)
---Old和New表示触发之前和触发之后的值-
mysql> delimiter ;
mysql> delete from ta where id=1;
Query OK, 1 row affected (0.05 sec)

mysql> select * from ta;
+----+
| id |
+----+
|  2 |
+----+
1 row in set (0.00 sec)

mysql> select * from tb;
+------+------+
| id   | aid  |
+------+------+
|    2 |    2 |
|    3 |    1 |
+------+------+
2 rows in set (0.00 sec)

```
###  语法部分

CREATE TRIGGER <触发器名称>  --触发器必须有名字，最多64个字符，可能后面会附有分隔符.它和MySQL中其他对象的命名方式基本相象.

{ BEFORE | AFTER }  --触发器有执行的时间设置：可以设置为事件发生前或后。

{ INSERT | UPDATE | DELETE }  --同样也能设定触发的事件：它们可以在执行insert、update或delete的过程中触发。

ON <表名称>  --触发器是属于某一个表的:当在这个表上执行插入、 更新或删除操作的时候就导致触发器的激活. 我们不能给同一张表的同一个事件安排两个触发器。

FOR EACH ROW  --触发器的执行间隔：FOR EACH ROW子句通知触发器 每隔一行执行一次动作，而不是对整个表执行一次。
<触发器SQL语句>  --触发器包含所要触发的SQL语句：这里的语句可以是任何合法的语句， 包括复合语句，但是这里的语句受的限制和函数的一样。

你必须拥有相当大的权限才能创建触发器（CREATE TRIGGER），如果你已经是Root用户，那么就足够了。这跟SQL的标准有所不同。
### 四、存储过程
mysql的触发器移植不太好调试起来也不如存储过程，所以这里就通过代替上述的存储过程来简单学习一下。
1. 概述
一提到存储过程可能就会引出另一个话题就是存储过程的优缺点，这里也不做讨论，一般别人问我我就这样回答你觉得它好你就用它。因为mysql中存储过程和函数的语法非常接近所以就放在一起，主要区别就是函数必须有返回值（return），并且函数的参数只有IN类型而存储过程有IN、OUT、INOUT这三种类型。
2. 创建存储过程和函数语法
```sql
CREATE PROCEDURE sp_name ([proc_parameter[,...]])
    [characteristic ...] routine_body
 
CREATE FUNCTION sp_name ([func_parameter[,...]])
    RETURNS type
    [characteristic ...] routine_body
    
    proc_parameter:
    [ IN | OUT | INOUT ] param_name type
    
    func_parameter:
    param_name type
 
type:
    Any valid MySQL data type
 
characteristic:
    LANGUAGE SQL
  | [NOT] DETERMINISTIC
  | { CONTAINS SQL | NO SQL | READS SQL DATA | MODIFIES SQL DATA }
  | SQL SECURITY { DEFINER | INVOKER }
  | COMMENT 'string'
 
routine_body:
    Valid SQL procedure statement or statements
```
3. 实例
```sql
mysql> DROP PROCEDURE IF EXISTS Pro_ta;
Query OK, 0 rows affected, 1 warning (0.08 sec)

mysql> delimiter $$
mysql> CREATE PROCEDURE Pro_ta(IN pdepid VARCHAR(20) )
    -> READS SQL DATA                                 
    -> SQL SECURITY INVOKER
    -> BEGIN
    -> delete from ta where id=pdepid;
    -> delete from tb where id=pdepid;
    -> END$$
Query OK, 0 rows affected (0.06 sec)
mysql> delimiter ;
```
* 解释：
```xml
proc_parameter中的每个参数由3部分组成。这3部分分别是输入输出类型、参数名称和参数类型。其形式如下：

[ IN | OUT | INOUT ] param_name type 
其中，IN表示输入参数；OUT表示输出参数； INOUT表示既可以是输入，也可以是输出； param_name参数是存储过程的参数名称；type参数指定存储过程的参数类型，该类型可以是MySQL数据库的任意数据类型。

LANGUAGE SQL：说明routine_body部分是由SQL语言的语句组成，这也是数据库系统默认的语言。

[NOT] DETERMINISTIC：指明存储过程的执行结果是否是确定的。DETERMINISTIC表示结果是确定的。每次执行存储过程时，相同的输入会得到相同的输出。NOT DETERMINISTIC表示结果是非确定的，相同的输入可能得到不同的输出。默认情况下，结果是非确定的

CONTAINS SQL | NO SQL | READS SQL DATA | MODIFIES SQL DATA指明子程序使用SQL语句的限制。CONTAINS SQL表示子程序包含SQL语句，但不包含读或写数据的语句NO SQL表示子程序中不包含SQL语句；READS SQL DATA表示子程序中包含读数据的语句；MODIFIES SQL DATA表示子程序中包含写数据的语句。默认情况下，系统会指定为CONTAINS SQL。

SQL SECURITY { DEFINER | INVOKER }：指明谁有权限来执行。DEFINER表示只有定义者自己才能够执行；INVOKER表示调用者可以执行。默认情况下，系统指定的权限是DEFINER。
```
### COMMENT 'string'：注释信息
```sql
mysql> call Pro_ta(1);---------调用存储过程，指定参数
Query OK, 0 rows affected (0.06 sec)

mysql> select * from ta;
+----+
| id |
+----+
|  2 |
|  2 |
+----+
2 rows in set (0.00 sec)

mysql> select * from tb;
+------+------+
| id   | aid  |
+------+------+
|    2 |    2 |
|    3 |    1 |
|    2 |    2 |
|    3 |    1 |
+------+------+
4 rows in set (0.00 sec)
```
### 五、 函数
1. 格式：
```sql
CREATE  

     [DEFINER = { user | CURRENT_USER }]  

     FUNCTION sp_name ([func_parameter[,...]])  

     RETURNS type  

     [characteristic ...] routine_body  

func_parameter:  

     param_name type     

type:  

     Any valid MySQL data type  

characteristic:  

     LANGUAGE SQL  

   | [NOT] DETERMINISTIC  

   | { CONTAINS SQL | NO SQL | READS SQL DATA | MODIFIES SQL DATA }  

   | SQL SECURITY { DEFINER | INVOKER }  

   | COMMENT 'string'  

routine_body:  

     Valid SQL procedure statement  

 

--自定义函数与存储过程的区别

1，函数方法的参数列表只允许IN类型的参数，并且不允许指定IN关键字

2，函数方法返回一个单一的值，值的类型在存储方法的头部定义

3，函数方法可以在SQL语句内部调用

4，函数方法不能返回结果集
```
3. mysql命令提示框函数使用

### 一、查看创建函数的功能是否开启：
```
mysql> show variables like '%func%';
+-----------------------------------------+-------+
| Variable_name | Value |
+-----------------------------------------+-------+
| log_bin_trust_function_creators | ON    |
+-----------------------------------------+-------+
1 row in set (0.02 sec)
```
## 二、如果Value处值为OFF，则需将其开启。
```sql
mysql> set global log_bin_trust_function_creators=1;
```
### 三、创建函数时，先选择数据库，
```
mysql> use xxx;
Database changed
mysql> delimiter $$
CREATE FUNCTION first_func(param1 varchar(5),parmam2 varchar(5),param3 varchar(10))
RETURNS TINYINT
BEGIN
   RETURN 1;
END
mysql> delimiter ;
------------------------------------需要当前用户有创建函数的权利，
```
### 四、测试：
```sql
mysql> select first_func('aaa','bbb','ccc');
+-------------------------------+
| first_func('aaa','bbb','ccc') |
+-------------------------------+
|                             1 |
+-------------------------------+
1 row in set (0.47 sec)
```
### 五、删除函数：
```sql
mysql> drop function first_func ;
Query OK, 0 rows affected (0.11 sec)
```
### 六、查看函数
```sql
show function status
```
显示数据库中所有函数的基本信息 
2)查看某个具体函数
```sql
mysql>show create function function;
```
* 下面创建一个名为name_from_employee的存储函数。代码如下：
```sql
CREATE  FUNCTION  name_from_employee (emp_id INT )  
          RETURNS VARCHAR(20)  
          BEGIN  
              RETURN  (SELECT  name  
              FROM  employee  
              WHERE  num=emp_id );  
          END 
```
<p>上述代码中，存储函数的名称为name_from_employee；该函数的参数为emp_id；返回值是VARCHAR类型。SELECT语句从employee表查询num值等于emp_id的记录，
并将该记录的name字段的值返回。代码的执行结果如下：
</p>
```sql
mysql> DELIMITER &&  
mysql> CREATE  FUNCTION  name_from_employee (emp_id INT )  
    -> RETURNS VARCHAR(20)  
    -> BEGIN  
    -> RETURN  (SELECT  name  
    -> FROM  employee  
    -> WHERE  num=emp_id );  
    -> END&&  
Query OK, 0 rows affected (0.00 sec)  
mysql> DELIMITER ; 
```
结果显示，存储函数已经创建成功。该函数的使用和MySQL内部函数的使用方法一样。

### 变量的使用

在存储过程和函数中，可以定义和使用变量。用户可以使用DECLARE关键字来定义变量。然后可以为变量赋值。这些变量的作用范围是BEGIN…END程序段中。本小节将讲解如何定义变量和为变量赋值。

1. 定义变量

MySQL中可以使用DECLARE关键字来定义变量。定义变量的基本语法如下：

DECLARE  var_name[,...]  type  [DEFAULT value] 
其中， DECLARE关键字是用来声明变量的；var_name参数是变量的名称，这里可以同时定义多个变量；type参数用来指定变量的类型；
DEFAULT value子句将变量默认值设置为value，没有使用DEFAULT子句时，默认值为NULL。

下面定义变量my_sql，数据类型为INT型，默认值为10。代码如下：
```sql
DECLARE  my_sql  INT  DEFAULT 10 ; 
```
2. 为变量赋值

MySQL中可以使用SET关键字来为变量赋值。SET语句的基本语法如下：

SET  var_name = expr [, var_name = expr] ... 
其中，SET关键字是用来为变量赋值的；var_name参数是变量的名称；expr参数是赋值表达式。一个SET语句可以同时为多个变量赋值，
各个变量的赋值语句之间用逗号隔开。

下面为变量my_sql赋值为30。代码如下：
```sql
SET  my_sql = 30 ; 
```
MySQL中还可以使用SELECT…INTO语句为变量赋值。其基本语法如下：
```sql
SELECT  col_name[,…]  INTO  var_name[,…]  
    FROM  table_name  WEHRE  condition 
```
其中，col_name参数表示查询的字段名称；var_name参数是变量的名称；table_name参数指表的名称；condition参数指查询条件。

】 下面从employee表中查询id为2的记录，将该记录的d_id值赋给变量my_sql。代码如下：
```sql
SELECT  d_id  INTO  my_sql  
        FROM  employee  WEHRE  id=2 ; 
 ```
###  定义条件和处理程序
<p>
定义条件和处理程序是事先定义程序执行过程中可能遇到的问题。并且可以在处理程序中定义解决这些问题的办法。这种方式可以提前预测可能出现的问题，并提出解决办法。这样可以增强程序处理问题的能力，避免程序异常停止。MySQL中都是通过DECLARE关键字来定义条件和处理程序。本小节中将详细讲解如何定义条件和处理程序。
</p>

1. 定义条件

MySQL中可以使用DECLARE关键字来定义条件。其基本语法如下：
```sql
DECLARE  condition_name  CONDITION  FOR  condition_value  
condition_value:  
      SQLSTATE [VALUE] sqlstate_value | mysql_error_code 
```
其中，condition_name参数表示条件的名称；condition_value参数表示条件的类型；sqlstate_value参数和mysql_error_code参数都可以表示MySQL的错误。例如ERROR 1146 (42S02)中，sqlstate_value值是42S02，mysql_error_code值是1146。

下面定义"ERROR 1146 (42S02)"这个错误，名称为can_not_find。可以用两种不同的方法来定义，代码如下：

* 方法一：使用sqlstate_value  
```sql
DECLARE  can_not_find  CONDITION  FOR  SQLSTATE  '42S02' ;  
```
* 方法二：使用mysql_error_code  
```sql
DECLARE  can_not_find  CONDITION  FOR  1146 ; 
```
2. 定义处理程序

MySQL中可以使用DECLARE关键字来定义处理程序。其基本语法如下：
```sql
DECLARE handler_type HANDLER FOR 
condition_value[,...] sp_statement  
handler_type:  
    CONTINUE | EXIT | UNDO  
condition_value:  
    SQLSTATE [VALUE] sqlstate_value |
condition_name  | SQLWARNING  
       | NOT FOUND  | SQLEXCEPTION  | mysql_error_code 
```
其中，handler_type参数指明错误的处理方式，该参数有3个取值。这3个取值分别是CONTINUE、EXIT和UNDO。CONTINUE表示遇到错误不进行处理，继续向下执行；EXIT表示遇到错误后马上退出；UNDO表示遇到错误后撤回之前的操作，MySQL中暂时还不支持这种处理方式。

* 注意：通常情况下，执行过程中遇到错误应该立刻停止执行下面的语句，并且撤回前面的操作。但是，MySQL中现在还不能支持UNDO操作。因此，遇到错误时最好执行EXIT操作。如果事先能够预测错误类型，并且进行相应的处理，那么可以执行CONTINUE操作。
<p>
condition_value参数指明错误类型，该参数有6个取值。sqlstate_value和mysql_error_code与条件定义中的是同一个意思。condition_name是DECLARE定义的条件名称。SQLWARNING表示所有以01开头的sqlstate_value值。NOT FOUND表示所有以02开头的sqlstate_value值。SQLEXCEPTION表示所有没有被SQLWARNING或NOT FOUND捕获的sqlstate_value值。sp_statement表示一些存储过程或函数的执行语句。
</P>

### 下面是定义处理程序的几种方式。代码如下：

* 方法一：捕获sqlstate_value  
```sql
DECLARE CONTINUE HANDLER FOR SQLSTATE '42S02'
SET @info='CAN NOT FIND';  
```
* 方法二：捕获mysql_error_code  
```sql
DECLARE CONTINUE HANDLER FOR 1146 SET @info='CAN NOT FIND';  
```
* 方法三：先定义条件，然后调用  
```sql
DECLARE  can_not_find  CONDITION  FOR  1146 ;  
DECLARE CONTINUE HANDLER FOR can_not_find SET 
@info='CAN NOT FIND';  
```
* 方法四：使用SQLWARNING  
```sql
DECLARE EXIT HANDLER FOR SQLWARNING SET @info='ERROR';  
```
* 方法五：使用NOT FOUND  
```sql
DECLARE EXIT HANDLER FOR NOT FOUND SET @info='CAN NOT FIND';  
```
* 方法六：使用SQLEXCEPTION  
```sql
DECLARE EXIT HANDLER FOR SQLEXCEPTION SET @info='ERROR'; 
```
* 解释如下：
<p>
上述代码是6种定义处理程序的方法。
第一种方法是捕获sqlstate_value值。如果遇到sqlstate_value值为42S02，执行CONTINUE操作，并且输出"CAN NOT FIND"信息。
第二种方法是捕获mysql_error_code值。如果遇到mysql_error_code值为1146，执行CONTINUE操作，并且输出"CAN NOT FIND"信息。
第三种方法是先定义条件，然后再调用条件。这里先定义can_not_find条件，遇到1146错误就执行CONTINUE操作。
第四种方法是使用SQLWARNING。SQLWARNING捕获所有以01开头的sqlstate_value值，然后执行EXIT操作，并且输出"ERROR"信息。
第五种方法是使用NOT FOUND。NOT FOUND捕获所有以02开头的sqlstate_value值，然后执行EXIT操作，并且输出"CAN NOT FIND"信息。
第六种方法是使用SQLEXCEPTION。SQLEXCEPTION捕获所有没有被SQLWARNING或NOT FOUND捕获的sqlstate_value值，然后执行EXIT操作，并且输出"ERROR"信息。</p>
