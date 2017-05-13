ql服务

/opt/mysql/init.d/mysql start



通过命令行登录管理MySQL服务器

/opt/mysql/bin/mysql -uroot -p'new-password'

1 mysql>create user repl; //创建新用户

2 //repl用户必须具有REPLICATION SLAVE权限，除此之外没有必要添加不必要的权限，密码为mysql。说明一下192.168.0.%，这个配置是指明repl用户所在服务器，这里%是通配符，表示192.168.0.0-192.168.0.255的Server都可以以repl用户登陆主服务器。当然你也可以指定固定Ip。

3 mysql> GRANT REPLICATION SLAVE ON *.* TO 'repl'@'192.168.0.%' IDENTIFIED BY 'mysql';

（2）找到MySQL安装文件夹修改my.Ini文件。mysql中有好几种日志方式，这不是今天的重点。我们只要启动二进制日志log-bin就ok。



 在[mysqld]下面增加下面几行代码





1 server-id=1   //给数据库服务的唯一标识，一般为大家设置服务器Ip的末尾号

2 log-bin=master-bin

3 log-bin-index=master-bin.index

重启MySQL服务后，查看

mysql> show master status;

+-------------------+----------+--------------+------------------+

| File              | Position | Binlog_Do_DB | Binlog_Ignore_DB |

+-------------------+----------+--------------+------------------+

| master-bin.000001 |      1770 |              |                  |

+-------------------+----------+--------------+------------------+

1 row in set (0.00 sec)

3、配置Slave从服务器（windows）



（1）找到MySQL安装文件夹修改my.ini文件，在[mysqld]下面增加下面几行代码





1 [mysqld]

2 server-id=2

3 relay-log-index=slave-relay-bin.index

4 relay-log=slave-relay-bin 

重启MySQL服务

（2）连接Master

执行同步SQL语句

mysql> change master to

master_host='192.168.106.128',

master_user='repl',

master_password='mysql',

master_log_file='master-bin.000001',

master_log_pos=1770;

（3）启动Slave



start slave;


