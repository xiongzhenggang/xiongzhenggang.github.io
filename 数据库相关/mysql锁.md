
https://blog.csdn.net/tanga842428/article/details/52748531

1. 相对于其他的数据库而言，MySQL的锁机制比较简单，最显著的特点就是不同的存储引擎支持不同的锁机制。根据不同的存储引擎，MySQL中锁的特性可以大致归纳如下

```
mysql 几种引擎的所具有的锁
  	      行锁 	表锁 	 页锁
MyISAM 	  	      √ 	 
BDB 	  	        √ 	 √
InnoDB   	 √ 	    √ 	 

 
开销、加锁速度、死锁、粒度、并发性能

表锁：开销小，加锁快；不会出现死锁；锁定力度大，发生锁冲突概率高，并发度最低
行锁：开销大，加锁慢；会出现死锁；锁定粒度小，发生锁冲突的概率低，并发度高
页锁：开销和加锁速度介于表锁和行锁之间；会出现死锁；锁定粒度介于表锁和行锁之间，并发度一般

注意：BDB已经被InnoDB所取代，只讨论MyISAM表锁和InnoDB行锁的问题
```
2. MyISAM表锁
```
MyISAM存储引擎只支持表锁，这也是MySQL开始几个版本中唯一支持的锁类型。随着应用对事务完整性和并发性要求的不断提高，MySQL才开始开发基于事务的存储引擎，后来慢慢出现了支持页锁的BDB存储引擎和支持行锁的InnoDB存储引擎（实际 InnoDB是单独的一个公司，现在已经被Oracle公司收购）。但是MyISAM的表锁依然是使用最为广泛的锁类型。本节将详细介绍MyISAM表锁的使用。
查询表级锁争用情况
可以通过检查table_locks_waited和table_locks_immediate状态变量来分析系统上的表锁定争夺：
```
```sal
mysql> show status like 'table%';  
+-----------------------+-------+  
| Variable_name         | Value |  
+-----------------------+-------+  
| Table_locks_immediate | 2979  |  
| Table_locks_waited    | 0     |  
+-----------------------+-------+  
2 rows in set (0.00 sec))
```
### MySQL表级锁的锁模式
```
MySQL的表级锁有两种模式：表共享读锁（Table Read Lock）和表独占写锁（Table Write Lock）。锁模式的兼容性如下表所示。
                                          MySQL中的表锁兼容性                
请求锁模式是否兼容当前锁模式  None 	读锁 	写锁
读锁 	                      是 	是 	否
写锁 	                      是 	否 	否
```
* 对MyISAM表的读操作，不会阻塞其他用户对同一表的读请求，但会阻塞对同一表的写请求；对 MyISAM表的写操作，则会阻塞其他用户对同一表的读和写操作；MyISAM表的读操作与写操作之间，以及写操作之间是串行的！根据如下表所示的例子可以知道，当一个线程获得对一个表的写锁后，只有持有锁的线程可以对表进行更新操作。其他线程的读、写操作都会等待，直到锁被释放为止。

            MyISAM存储引擎的写阻塞读例子
            
<table>
  <tr> 
    <th>session_1</th> 
    <th> session_2</th> 
  </tr> 
  <tr> 
    <th> 获得表film_text的WRITE锁定<br/>
    mysql> lock table film_text write;</br>
     Query OK, 0 rows affected (0.00 sec) </th> 
    <th>                 </th>
    <th>当前session对锁定表的查询、更新、插入操作都可以执行：</br>
    mysql> select film_id,title from film_text where film_id = 1001;
+---------+-------------+
| film_id | title       |
+---------+-------------+
| 1001    | Update Test |
+---------+-------------+
1 row in set (0.00 sec)</br>
mysql> insert into film_text (film_id,title) values(1003,'Test');</br>
Query OK, 1 row affected (0.00 sec)</br>
mysql> update film_text set title = 'Test' where film_id = 1001;</br>
Query OK, 1 row affected (0.00 sec)</br>
Rows matched: 1  Changed: 1  Warnings: 0</th>
<th>其他session对锁定表的查询被阻塞，需要等待锁被释放：</br>
mysql> select film_id,title from film_text where film_id = 1001;
等待</th>
  </tr> 
</table>

|session_1     | session_2    |
| --------      | -----:      |
| 获得表film_text的WRITE锁定
  mysql> lock table film_text write;
  Query OK, 0 rows affected (0.00 sec)      |                   |
|当前session对锁定表的查询、更新、插入操作都可以执行：
mysql> select film_id,title from film_text where film_id = 1001;
+---------+-------------+
| film_id | title       |
+---------+-------------+
| 1001    | Update Test |
+---------+-------------+
1 row in set (0.00 sec)
mysql> insert into film_text (film_id,title) values(1003,'Test');
Query OK, 1 row affected (0.00 sec)
mysql> update film_text set title = 'Test' where film_id = 1001;
Query OK, 1 row affected (0.00 sec)
Rows matched: 1  Changed: 1  Warnings: 0  
| 其他session对锁定表的查询被阻塞，需要等待锁被释放：mysql> select film_id,title from film_text where film_id = 1001;等待  |

 


