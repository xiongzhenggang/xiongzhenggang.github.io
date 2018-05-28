## mysql 锁详解

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
```
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

```
------------------session1--------------------------------------|--------------------session2------------------------------
   获得表film_text的WRITE锁定                                    |
mysql> lock table film_text write;                              |
Query OK, 0 rows affected (0.00 sec)                            |
--------------------------------------------------------------------------------------------------------------------------
当前session可以查询该表记录                                         |其他session也可以查询该表的记录
                                                                   |mysql> select film_id,title from film_text where film_id = 1001;
                                                                   |1 row in set (0.00 sec)
mysql> select film_id,title from film_text where film_id = 1001;   |
+---------+------------------+                                     |
| film_id | title            |                                     |
+---------+------------------+                                     |
| 1001    | ACADEMY DINOSAUR |                                     |
+---------+------------------+
1 row in set (0.00 sec)
------------------------------------------------------------------------------------------------------------------------------
当前session不能查询没有锁定的表                                     |其他session更新锁定表会等待获得锁：
mysql> select film_id,title from film where film_id = 1001;       |mysql> update film_text set title = 'Test' where fil
ERROR 1100 (HY000): Table 'film' was not locked with LOCK TABLES  |m_id = 1001;          等待
----------------------------------------------------------------------------------------------------------------------------
当前session中插入或者更新锁定的表都会提示错误：                                           |
mysql> insert into film_text (film_id,title) values(1002,'Test');                      |
ERROR 1099 (HY000): Table 'film_text' was locked with a READ lock and can't be updated  |
mysql> update film_text set title = 'Test' where film_id = 1001;                        |
ERROR 1099 (HY000): Table 'film_text' was locked with a READ lock and can't be updated  |
-----------------------------------------------------------------------------------------------------------------------------
会导致其session阻塞知道session1 释放锁后释放锁：    |Session2获得锁，更新操作完成：
mysql> unlock tables;                             |mysql> update film_text set title = 'Test' where film_id = 1001;
Query OK, 0 rows affected (0.00 sec)              |Query OK, 1 row affected (1 min 0.71 sec)
才会继续                                          |  Rows matched: 1  Changed: 1  Warnings: 0

```
### 如何加表锁

```
MyISAM在执行查询语句（SELECT）前，会自动给涉及的所有表加读锁，在执行更新操作（UPDATE、DELETE、INSERT等）前，会自动给涉及的表加写锁，
这个过程并不需要用户干预，因此，用户一般不需要直接用LOCK TABLE命令给MyISAM表显式加锁。在示例中，显式加锁基本上是为了方便而已，并非必须如此。
给MyISAM表显示加锁，一般是为了在一定程度模拟事务操作，实现对某一时间点多个表的一致性读取。例如，有一个订单表orders，其中记录有各订单的总金额total，
同时还有一个订单明细表order_detail，其中记录有各订单每一产品的金额小计 subtotal，假设我们需要检查这两个表的金额合计是否相符，可能就需要执行如下两条SQL：

    Select sum(total) from orders;  
    Select sum(subtotal) from order_detail;  
    这时，如果不先给两个表加锁，就可能产生错误的结果，因为第一条语句执行过程中，order_detail表可能已经发生了改变。因此，正确的方法应该是：  
    Lock tables orders read local, order_detail read local;  
    Select sum(total) from orders;  
    Select sum(subtotal) from order_detail;  
    Unlock tables;  
```
* 说明以下两点内容。
1. 上面的例子在LOCK TABLES时加了“local”选项，其作用就是在满足MyISAM表并发插入条件的情况下，允许其他用户在表尾并发插入记录，有关MyISAM表的并发插入问题
2. 在用LOCK TABLES给表显式加表锁时，必须同时取得所有涉及到表的锁，并且MySQL不支持锁升级。也就是说，在执行LOCK TABLES后，只能访问显式加锁的这些表，不能访问未加锁的表；同时，如果加的是读锁，那么只能执行查询操作，而不能执行更新操作。其实，在自动加锁的情况下也基本如此，MyISAM总是一次获得SQL语句所需要的全部锁。这也正是MyISAM表不会出现死锁（Deadlock Free）的原因。
```
一个session使用LOCK TABLE命令给表film_text加了读锁，这个session可以查询锁定表中的记录，但更新或访问其他表都会提示错误；同时，另外一个session可以查询表中的记录，但更新就会出现锁等待
可以手动测试。。。

注意，当使用LOCK TABLES时，不仅需要一次锁定用到的所有表，而且，同一个表在SQL语句中出现多少次，就要通过与SQL语句中相同的别名锁定多少次，否则也会出错！举例说明如下。 
```

### 并发插入（Concurrent Inserts）
```
上文提到过MyISAM表的读和写是串行的，但这是就总体而言的。在一定条件下，MyISAM表也支持查询和插入操作的并发进行。
MyISAM存储引擎有一个系统变量concurrent_insert，专门用以控制其并发插入的行为，其值分别可以为0、1或2。
当concurrent_insert设置为0时，不允许并发插入。
当concurrent_insert设置为1时，如果MyISAM表中没有空洞（即表的中间没有被删除的行），MyISAM允许在一个进程读表的同时，另一个进程从表尾插入记录。这也是MySQL的默认设置。
当concurrent_insert设置为2时，无论MyISAM表中有没有空洞，都允许在表尾并发插入记录。


可以利用MyISAM存储引擎的并发插入特性，来解决应用中对同一表查询和插入的锁争用。例如，将concurrent_insert系统变量设为2，总是允许并发插入；同时，通过定期在系统空闲时段执行 OPTIMIZE TABLE语句来整理空间碎片，收回因删除记录而产生的中间空洞。
```
* MyISAM的锁调度
```
前面讲过，MyISAM存储引擎的读锁和写锁是互斥的，读写操作是串行的。那么，一个进程请求某个 MyISAM表的读锁，同时另一个进程也请求同一表的写锁，MySQL如何处理呢？答案是写进程先获得锁。不仅如此，即使读请求先到锁等待队列，写请求后到，写锁也会插到读锁请求之前！这是因为MySQL认为写请求一般比读请求要重要。这也正是MyISAM表不太适合于有大量更新操作和查询操作应用的原因，因为，大量的更新操作会造成查询操作很难获得读锁，从而可能永远阻塞。这种情况有时可能会变得非常糟糕！幸好我们可以通过一些设置来调节MyISAM 的调度行为。

    通过指定启动参数low-priority-updates，使MyISAM引擎默认给予读请求以优先的权利。
    通过执行命令SET LOW_PRIORITY_UPDATES=1，使该连接发出的更新请求优先级降低。
    通过指定INSERT、UPDATE、DELETE语句的LOW_PRIORITY属性，降低该语句的优先级。

虽然上面3种方法都是要么更新优先，要么查询优先的方法，但还是可以用其来解决查询相对重要的应用（如用户登录系统）中，读锁等待严重的问题。
另外，MySQL也提供了一种折中的办法来调节读写冲突，即给系统参数max_write_lock_count设置一个合适的值，当一个表的读锁达到这个值后，MySQL就暂时将写请求的优先级降低，给读进程一定获得锁的机会。
上面已经讨论了写优先调度机制带来的问题和解决办法。这里还要强调一点：一些需要长时间运行的查询操作，也会使写进程“饿死”！因此，应用中应尽量避免出现长时间运行的查询操作，不要总想用一条SELECT语句来解决问题，因为这种看似巧妙的SQL语句，往往比较复杂，执行时间较长，在可能的情况下可以通过使用中间表等措施对SQL语句做一定的“分解”，使每一步查询都能在较短时间完成，从而减少锁冲突。如果复杂查询不可避免，应尽量安排在数据库空闲时段执行，比如一些定期统计可以安排在夜间执行。
```
### InnoDB锁问题
InnoDB与MyISAM的最大不同有两点：一是支持事务（TRANSACTION）；二是采用了行级锁。行级锁与表级锁本来就有许多不同之处，另外，事务的引入也带来了一些新问题。下面我们先介绍一点背景知识，然后详细讨论InnoDB的锁问题。
背景知识

1．事务（Transaction）及其ACID属性

* 原子性（Atomicity）：事务是一个原子操作单元，其对数据的修改，要么全都执行，要么全都不执行。
*一致性（Consistent）：在事务开始和完成时，数据都必须保持一致状态。这意味着所有相关的数据规则都必须应用于事务的修改，以保持数据的完整性；事务结束时，所有的内部数据结构（如B树索引或双向链表）也都必须是正确的。
* 隔离性（Isolation）：数据库系统提供一定的隔离机制，保证事务在不受外部并发操作影响的“独立”环境执行。这意味着事务处理过程中的中间状态对外部是不可见的，反之亦然。
* 持久性（Durable）：事务完成之后，它对于数据的修改是永久性的，即使出现系统故障也能够保持。

2．并发事务处理带来的问题
相对于串行处理来说，并发事务处理能大大增加数据库资源的利用率，提高数据库系统的事务吞吐量，从而可以支持更多的用户。但并发事务处理也会带来一些问题，主要包括以下几种情况。
* 更新丢失（Lost Update）：当两个或多个事务选择同一行，然后基于最初选定的值更新该行时，由于每个事务都不知道其他事务的存在，就会发生丢失更新问题－－最后的更新覆盖了由其他事务所做的更新。例如，两个编辑人员制作了同一文档的电子副本。每个编辑人员独立地更改其副本，然后保存更改后的副本，这样就覆盖了原始文档。最后保存其更改副本的编辑人员覆盖另一个编辑人员所做的更改。如果在一个编辑人员完成并提交事务之前，另一个编辑人员不能访问同一文件，则可避免此问题。
* 脏读（Dirty Reads）：一个事务正在对一条记录做修改，在这个事务完成并提交前，这条记录的数据就处于不一致状态；这时，另一个事务也来读取同一条记录，如果不加控制，第二个事务读取了这些“脏”数据，并据此做进一步的处理，就会产生未提交的数据依赖关系。这种现象被形象地叫做"脏读"。
不可重复读（Non-Repeatable Reads）：一个事务在读取某些数据后的某个时间，再次读取以前读过的数据，却发现其读出的数据已经发生了改变、或某些记录已经被删除了！这种现象就叫做“不可重复读”。
* 幻读（Phantom Reads）：一个事务按相同的查询条件重新读取以前检索过的数据，却发现其他事务插入了满足其查询条件的新数据，这种现象就称为“幻读”。

3. 事务隔离级别
在上面讲到的并发事务处理带来的问题中，“更新丢失”通常是应该完全避免的。但防止更新丢失，并不能单靠数据库事务控制器来解决，需要应用程序对要更新的数据加必要的锁来解决，因此，防止更新丢失应该是应用的责任。
“脏读”、“不可重复读”和“幻读”，其实都是数据库读一致性问题，必须由数据库提供一定的事务隔离机制来解决。数据库实现事务隔离的方式，基本上可分为以下两种。

一种是在读取数据前，对其加锁，阻止其他事务对数据进行修改。
另一种是不用加任何锁，通过一定机制生成一个数据请求时间点的一致性数据快照（Snapshot)，并用这个快照来提供一定级别（语句级或事务级）的一致性读取。从用户的角度来看，好像是数据库可以提供同一数据的多个版本，因此，这种技术叫做数据多版本并发控制（MultiVersion Concurrency Control，简称MVCC或MCC），也经常称为多版本数据库。

数据库的事务隔离越严格，并发副作用越小，但付出的代价也就越大，因为事务隔离实质上就是使事务在一定程度上 “串行化”进行，这显然与“并发”是矛盾的。同时，不同的应用对读一致性和事务隔离程度的要求也是不同的，比如许多应用对“不可重复读”和“幻读”并不敏感，可能更关心数据并发访问的能力。
为了解决“隔离”与“并发”的矛盾，ISO/ANSI SQL92定义了4个事务隔离级别，每个级别的隔离程度不同，允许出现的副作用也不同，应用可以根据自己的业务逻辑要求，通过选择不同的隔离级别来平衡 “隔离”与“并发”的矛盾。下表很好地概括了这4个隔离级别的特性。

```
                              4种隔离级别比较
读数据一致性及允许的并发副作用 隔离级别         读数据一致性 	                   脏读 	不可重复读 	幻读
未提交读（Read uncommitted）          最低级别，只能保证不读取物理上损坏的数据 	   是 	    是 	      是
已提交度（Read committed）                    语句级 	                          否 	   是 	       是
可重复读（Repeatable read）                   事务级 	                          否 	   否 	       是
可序列化（Serializable）                     最高级别，事务级 	                  否 	   否 	       否
  ```
最后要说明的是：各具体数据库并不一定完全实现了上述4个隔离级别，例如，Oracle只提供Read committed和Serializable两个标准隔离级别，另外还提供自己定义的Read only隔离级别；SQL Server除支持上述ISO/ANSI SQL92定义的4个隔离级别外，还支持一个叫做“快照”的隔离级别，但严格来说它是一个用MVCC实现的Serializable隔离级别。MySQL 支持全部4个隔离级别，但在具体实现时，有一些特点，比如在一些隔离级别下是采用MVCC一致性读，但某些情况下又不是，这些内容在后面的章节中将会做进一步介绍。

获取InnoDB行锁争用情况    
可以通过检查InnoDB_row_lock状态变量来分析系统上的行锁的争夺情况：
```
    mysql> show status like 'innodb_row_lock%';  
    +-------------------------------+-------+  
    | Variable_name                 | Value |  
    +-------------------------------+-------+  
    | InnoDB_row_lock_current_waits | 0     |  
    | InnoDB_row_lock_time          | 0     |  
    | InnoDB_row_lock_time_avg      | 0     |  
    | InnoDB_row_lock_time_max      | 0     |  
    | InnoDB_row_lock_waits         | 0     |  
    +-------------------------------+-------+  
    5 rows in set (0.01 sec)  
    如果发现锁争用比较严重，如InnoDB_row_lock_waits和InnoDB_row_lock_time_avg的值比较高，还可以通过设置InnoDB Monitors来进一步观察发生锁冲突的表、数据行等，并分析锁争用的原因。
```
```
具体方法如下：  
mysql> CREATE TABLE innodb_monitor(a INT) ENGINE=INNODB;  
Query OK, 0 rows affected (0.14 sec)  
然后就可以用下面的语句来进行查看：  
mysql> Show innodb status\G;  
*************************** 1. row ***************************  
  Type: InnoDB  
  Name:  
Status:  
…  
…  
------------  
TRANSACTIONS  
------------  
Trx id counter 0 117472192  
Purge done for trx's n:o < 0 117472190 undo n:o < 0 0  
History list length 17  
Total number of lock structs in row lock hash table 0  
LIST OF TRANSACTIONS FOR EACH SESSION:  
---TRANSACTION 0 117472185, not started, process no 11052, OS thread id 1158191456  
MySQL thread id 200610, query id 291197 localhost root  
---TRANSACTION 0 117472183, not started, process no 11052, OS thread id 1158723936  
MySQL thread id 199285, query id 291199 localhost root  
Show innodb status  
…  
监视器可以通过发出下列语句来停止查看：  
mysql> DROP TABLE innodb_monitor;  
Query OK, 0 rows affected (0.05 sec)
```
设置监视器后，在SHOW INNODB STATUS的显示内容中，会有详细的当前锁等待的信息，包括表名、锁类型、锁定记录的情况等，便于进行进一步的分析和问题的确定。打开监视器以后，默认情况下每15秒会向日志中记录监控的内容，如果长时间打开会导致.err文件变得非常的巨大，所以用户在确认问题原因之后，要记得删除监控表以关闭监视器，或者通过使用“--console”选项来启动服务器以关闭写日志文件。
```
InnoDB的行锁模式及加锁方法
InnoDB实现了以下两种类型的行锁。

    共享锁（S）：允许一个事务去读一行，阻止其他事务获得相同数据集的排他锁。
    排他锁（X)：允许获得排他锁的事务更新数据，阻止其他事务取得相同数据集的共享读锁和排他写锁。另外，为了允许行锁和表锁共存，实现多粒度锁机制，InnoDB还有两种内部使用的意向锁（Intention Locks），这两种意向锁都是表锁。

    意向共享锁（IS）：事务打算给数据行加行共享锁，事务在给一个数据行加共享锁前必须先取得该表的IS锁。
    意向排他锁（IX）：事务打算给数据行加行排他锁，事务在给一个数据行加排他锁前必须先取得该表的IX锁。
```
上述锁模式的兼容情况具体如下表所示。
```
                                         InnoDB行锁模式兼容性列表
              请求锁模式是否兼容当前锁模式                X    	IX   	S   	IS
                              X 	                     冲突 	冲突 	冲突 	冲突
                              IX                       冲突 	兼容 	冲突 	兼容
                               S 	                     冲突 	冲突 	兼容 	兼容
                              IS 	                     冲突 	兼容 	兼容 	兼容
                                         
```
如果一个事务请求的锁模式与当前的锁兼容，InnoDB就将请求的锁授予该事务；反之，如果两者不兼容，该事务就要等待锁释放。
意向锁是InnoDB自动加的，不需用户干预。对于UPDATE、DELETE和INSERT语句，InnoDB会自动给涉及数据集加排他锁（X)；对于普通SELECT语句，InnoDB不会加任何锁；事务可以通过以下语句显示给记录集加共享锁或排他锁。

*共享锁（S）：SELECT * FROM table_name WHERE ... LOCK IN SHARE MODE。
*排他锁（X)：SELECT * FROM table_name WHERE ... FOR UPDATE。

用SELECT ... IN SHARE MODE获得共享锁，主要用在需要数据依存关系时来确认某行记录是否存在，并确保没有人对这个记录进行UPDATE或者DELETE操作。但是如果当前事务也需要对该记录进行更新操作，则很有可能造成死锁，对于锁定行记录后需要进行更新操作的应用，应该使用SELECT... FOR UPDATE方式获得排他锁。
在如下表所示的例子中，使用了SELECT ... IN SHARE MODE加锁后再更新记录，看看会出现什么情况，其中actor表的actor_id字段为主键。
```

```

InnoDB行锁实现方式
*InnoDB行锁是通过给索引上的索引项加锁来实现的，这一点MySQL与Oracle不同，后者是通过在数据块中对相应数据行加锁来实现的。InnoDB这种行锁实现特点意味着：只有通过索引条件检索数据，InnoDB才使用行级锁，否则，InnoDB将使用表锁！
在实际应用中，要特别注意InnoDB行锁的这一特性，不然的话，可能导致大量的锁冲突，从而影响并发性能。下面通过一些实际例子来加以说明。

（1）在不通过索引条件查询的时候，InnoDB确实使用的是表锁，而不是行锁。
在如下所示的例子中，开始tab_no_index表没有索引：
```
    mysql> create table tab_no_index(id int,name varchar(10)) engine=innodb;  
    Query OK, 0 rows affected (0.15 sec)  
    mysql> insert into tab_no_index values(1,'1'),(2,'2'),(3,'3'),(4,'4');  
    Query OK, 4 rows affected (0.00 sec)  
    Records: 4  Duplicates: 0  Warnings: 0  
 ```
 InnoDB存储引擎的表在不使用索引时使用表锁例子
 ```
 ------------------------------------------------------------------------------------------------------
 session_1 	                                                      session_2
 mysql> set autocommit=0;                                         mysql> set autocommit=0;
Query OK, 0 rows affected (0.00 sec)                              Query OK, 0 rows affected (0.00 sec)
mysql> select * from tab_no_index where id = 1 ;                  mysql> select * from tab_no_index where id = 2 ;
+------+------+                                                   +------+------+
| id   | name |                                                   | id   | name |
+------+------+                                                   +------+------+
| 1    | 1    |                                                   | 2    | 2    |
+------+------+                                                   +------+------+
1 row in set (0.00 sec)                                           1 row in set (0.00 sec)
-----------------------------------------------------------------------------------------------
mysql> select * from tab_no_index where id = 1 for update;
+------+------+
| id   | name |
+------+------+
| 1    | 1    |
+------+------+
1 row in set (0.00 sec)
---------------------------------------------------------------------------------------------
                                                            mysql> select * from tab_no_index where id = 2 for update;
                                                            等待..
                                                            
 
 ```
在如上表所示的例子中，看起来session_1只给一行加了排他锁，但session_2在请求其他行的排他锁时，却出现了锁等待！原因就是在没有索引的情况下，InnoDB只能使用表锁。当我们给其增加一个索引后，InnoDB就只锁定了符合条件的行，如下表所示。
创建tab_with_index表，id字段有普通索引：
```
mysql> create table tab_with_index(id int,name varchar(10)) engine=innodb;  
Query OK, 0 rows affected (0.15 sec)  
mysql> alter table tab_with_index add index id(id);  
Query OK, 4 rows affected (0.24 sec)  
Records: 4  Duplicates: 0  Warnings: 0 
```
   InnoDB存储引擎的表在使用索引时使用行锁例子 略。。。

（2）由于MySQL的行锁是针对索引加的锁，不是针对记录加的锁，所以虽然是访问不同行的记录，但是如果是使用相同的索引键，是会出现锁冲突的。应用设计的时候要注意这一点。
在如下表所示的例子中，表tab_with_index的id字段有索引，name字段没有索引：
```
    mysql> alter table tab_with_index drop index name;  
    Query OK, 4 rows affected (0.22 sec)  
    Records: 4  Duplicates: 0  Warnings: 0  
    mysql> insert into tab_with_index  values(1,'4');  
    Query OK, 1 row affected (0.00 sec)  
    mysql> select * from tab_with_index where id = 1;  
    +------+------+  
    | id   | name |  
    +------+------+  
    | 1    | 1    |  
    | 1    | 4    |  
    +------+------+  
    2 rows in set (0.00 sec)  
```
（3）当表有多个索引的时候，不同的事务可以使用不同的索引锁定不同的行，另外，不论是使用主键索引、唯一索引或普通索引，InnoDB都会使用行锁来对数据加锁。
在如下表所示的例子中，表tab_with_index的id字段有主键索引，name字段有普通索引：
```
    mysql> alter table tab_with_index add index name(name);  
    Query OK, 5 rows affected (0.23 sec)  
    Records: 5  Duplicates: 0  Warnings: 0  
```
<table border="1">
  <tr>
    <th>session_1</th>
    <th>session_2</th>
  </tr>
  <tr>
    <td>mysql> set autocommit=0;
Query OK, 0 rows affected (0.00 sec)</td>
    <td>mysql> set autocommit=0;
Query OK, 0 rows affected (0.00 sec)</td>
  </tr>
   <tr>
    <td>
 mysql> select * from tab_with_index where id = 1 for update;
</br>+------+------+
</br>| id   | name |
</br>+------+------+
</br>| 1    | 1    |
</br>| 1    | 4    |
</br>+------+------+
</br>2 rows in set (0.00 sec)
     </td>
    <td> </td>
 </tr>
 <tr>
   <td></td>
   <td>
  Session_2使用name的索引访问记录，因为记录没有被索引，所以可以获得锁：
mysql> select * from tab_with_index where name = '2' for update;
</br>+------+------+
</br>| id   | name |
</br>+------+------+
</br>| 2    | 2    |
</br>+------+------+
1 row in set (0.00 sec)
  </td>
  </tr>
  <tr>
 <td></td>
 <td> 	
由于访问的记录已经被session_1锁定，所以等待获得锁。：
mysql> select * from tab_with_index where name = '4' for update;</td>
  </tr>
  </tr>
</table>

（4）即便在条件中使用了索引字段，但是否使用索引来检索数据是由MySQL通过判断不同执行计划的代价来决定的，如果MySQL认为全表扫描效率更高，比如对一些很小的表，它就不会使用索引，这种情况下InnoDB将使用表锁，而不是行锁。因此，在分析锁冲突时，别忘了检查SQL的执行计划，以确认是否真正使用了索引。


在下面的例子中，检索值的数据类型与索引字段不同，虽然MySQL能够进行数据类型转换，但却不会使用索引，从而导致InnoDB使用表锁。通过用explain检查两条SQL的执行计划，我们可以清楚地看到了这一点。
例子中tab_with_index表的name字段有索引，但是name字段是varchar类型的，如果where条件中不是和varchar类型进行比较，则会对name进行类型转换，而执行的全表扫描。

```
mysql> alter table tab_no_index add index name(name);  
Query OK, 4 rows affected (8.06 sec)  
Records: 4  Duplicates: 0  Warnings: 0  
mysql> explain select * from tab_with_index where name = 1 \G  
*************************** 1. row ***************************  
           id: 1  
  select_type: SIMPLE  
        table: tab_with_index  
         type: ALL  
possible_keys: name  
          key: NULL  
      key_len: NULL  
          ref: NULL  
         rows: 4  
        Extra: Using where  
1 row in set (0.00 sec)  
mysql> explain select * from tab_with_index where name = '1' \G  
*************************** 1. row ***************************  
           id: 1  
  select_type: SIMPLE  
        table: tab_with_index  
         type: ref  
possible_keys: name  
          key: name  
      key_len: 23  
          ref: const  
         rows: 1  
        Extra: Using where  
1 row in set (0.00 sec) 
```

https://blog.csdn.net/tanga842428/article/details/52748531
