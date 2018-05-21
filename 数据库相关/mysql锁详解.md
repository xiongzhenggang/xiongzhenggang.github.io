
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
