1、数据库结构

每一个redis服务器内部的数据结构都是一个redisDb[]，该数组的大小可以在redis.conf中配置（"database 16"，默认为16），而我们所有的缓存操作（set/hset/get等）都是在redisDb[]中的一个redisDb（库）上进行操作，这个redisDb默认是redisDb[0]。

注意：

可以通过"select 1"来选择接下来的操作在redisDb[1]上进行操作

在实际使用中，我们只在redisDb[0]上操作，因为

redis没有获取当前是在哪一个redisDb上操作的函数，所以很容易才select多次之后，我们就不知道在哪一个库上了，而且既然是只在redisDb[0]上进行操作，那么"database"就可以设置为1了，

该参数设置为1后，不仅可以将原有的其他redisDb所占的内存给了redisDb[0]，在的"定期删除"策略中，我们也只扫描一个redisDb就可以了。

2、读写原理

在每一个redisDb中都以一个dict（字典）用于存储"key-value"。

例子：

假设在redis中执行了如下四条命令并且没有执行任何的select，即默认选择在redisDb[0]上操作
```sh
set msg "hello nana"

rpush mylist "a" "b" "c"

hset book name "lover"

hset book author "nana"

```
读写时所进行的维护工作


在读取一个key（读写操作都需要读取key）后，


服务器更新缓存命中次数与不命中次数

更新该key的最后一次使用时间

检测该key是否过期

写计数器+1，用于持久化

### Redis持久化--RDB+AOF

* 1、Redis两种持久化方式

RDB

执行机制：快照，直接将databases中的key-value的二进制形式存储在了rdb文件中

优点：性能较高（因为是快照，且执行频率比aof低，而且rdb文件中直接存储的是key-values的二进制形式，对于恢复数据也快）

缺点：在save配置条件之间若发生宕机，此间的数据会丢失

AOF

执行机制：将对数据的每一条修改命令追加到aof文件

优点：数据不容易丢失

缺点：性能较低（每一条修改操作都要追加到aof文件，执行频率较RDB要高，而且aof文件中存储的是命令，对于恢复数据来讲需要逐行执行命令，所以恢复慢）

* 2、RDB

实际中使用的配置（在redis.conf）

```conf
#发生以下三种的任何一种都会将数据库的缓存内容写入到rdb文件中去(写入的方式是bgsave)

#若将下述的三条命令都注释掉，则禁止使用rdb

save 900 1      #900s后至少有一个key发生了变化

save 300 10      #300s后至少有10个key发生了变化

save 60 10000    #60s后至少有10000个key发生了变化



#当后台RDB进程导出快照（一部分的key-value）到rdb文件这个过程出错时（即最后一次的后台保存失败时），

#redis主进程是否还接受向数据库写数据

#该种方式会让用户知道在数据持久化到硬盘时出错了（相当于一种监控）；

#如果安装了很好的redis持久化监控，可设置为"no"

stop-writes-on-bgsave-error yes



#使用LZF压缩字符串，然后写到rdb文件中去

#如果希望RDB进程节省一点CPU时间，设置为no，但是可能最后的rdb文件会很大

rdbcompression yes



#在redis重启后，从rdb文件向内存写数据之前，是否先检测该rdb文件是否损坏(根据rdb文件中的校验和check_sum)

rdbchecksum yes

#设置rdb文件名

dbfilename dump.rdb

3、AOF

实际中使用的配置（在redis.conf）


# 是否打开aof日志功能（appendonly yes）

appendonly no

# aof文件的存放路径与文件名称

# appendfilename appendonly.aof

#每一个命令，都立即同步到aof文件中去（很安全，但是速度慢，因为每一个命令都会进行一次磁盘操作）

# appendfsync always

#每秒将数据写一次到aof文件

appendfsync everysec

#将写入工作交给操作系统，由操作系统来判断缓冲区大小，统一写到aof文件（速度快，但是同步频率低，容易丢数据） 

# appendfsync no

# 在RDB持久化数据的时候，此时的aof操作是否停止，若为yes则停止

# 在停止的这段时间内，执行的命令会写入内存队列，等RDB持久化完成后，统一将这些命令写入aof文件

# 该参数的配置是考虑到RDB持久化执行的频率低，但是执行的时间长，而AOF执行的频率高，执行的时间短，

# 若同时执行两个子进程（RDB子进程、AOF子进程）效率会低（两个子进程都是磁盘读写）

# 但是若改为yes可能造成的后果是，由于RDB持久化执行时间长，在这段时间内有很多命令写入了内存队列，

# 最后导致队列放不下，这样AOF写入到AOF文件中的命令可能就少了很多

# 在恢复数据的时候，根据aof文件恢复就会丢很多数据

# 所以，选择no就好

no-appendfsync-on-rewrite no

# AOF重写：把内存中的数据逆化成命令，然后将这些命令重新写入aof文件

# 重写的目的：假设在我们在内存中对同一个key进行了100次操作，最后该key的value是100，

# 那么在aof中就会存在100条命令日志，这样的话，有两个缺点：

# 1）AOF文件过大，占据硬盘空间 2）根据AOF文件恢复数据极慢（需要执行100条命令）

# 如果我们将内存中的该key逆化成"set key 100"，然后写入aof文件，

# 那么aof文件的大小会大幅度减少，而且根据aof文件恢复数据很快（只需要执行1条命令）

# 注意：下边两个约束都要满足的条件下，才会发生aof重写；

# 假设没有第二个，那么在aof的前期，只要稍微添加一些数据，就发生aof重写

# 当aof的增长的百分比是原来的100%（即是原来大小的2倍，例如原来是100m，下一次重写是当aof文件是200m的时候），AOF重写

auto-aof-rewrite-percentage 100  

auto-aof-rewrite-min-size 64mb   #AOF重写仅发生在当aof文件大于64m时

#设置rdb文件的存储目录

dir ./
```
