### redis服务器的集群主从配置，从redis的安装到实例开启，到集群创建。集群配置3个master和3
个node（官方集群最低要求）:
* 为方便使用单机搭建,搭建后启动如下
```
[root@localhost redis-4.0.6]# ps -ef | grep redis
root     31420     1  0 00:23 ?        00:00:00 src/redis-server 0.0.0.0:7000 [cluster]
root     31442     1  0 00:23 ?        00:00:00 src/redis-server 0.0.0.0:7001 [cluster]
root     32088     1  0 00:35 ?        00:00:00 src/redis-server 0.0.0.0:7002 [cluster]
root     32113     1  0 00:36 ?        00:00:00 src/redis-server 0.0.0.0:7003 [cluster]
root     32125     1  0 00:36 ?        00:00:00 src/redis-server 0.0.0.0:7004 [cluster]
root     32137     1  0 00:36 ?        00:00:00 src/redis-server 0.0.0.0:7005 [cluster]
```
1. [官网](https://redis.io/download)下载redis，可参考官网安装方式
```
$ wget http://download.redis.io/releases/redis-4.0.6.tar.gz
$ tar xzf redis-4.0.6.tar.gz
$ cd redis-4.0.6
$ make
```

