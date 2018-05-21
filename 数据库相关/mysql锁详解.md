
https://blog.csdn.net/tanga842428/article/details/52748531

1. 相对于其他的数据库而言，MySQL的锁机制比较简单，最显著的特点就是不同的存储引擎支持不同的锁机制。根据不同的存储引擎，MySQL中锁的特性可以大致归纳如下

```
mysql 几种引擎的所具有的锁
  	      行锁 	表锁 	页锁
MyISAM 	  	      √ 	 
BDB 	  	        √ 	√
InnoDB   	√ 	√ 	 
```
