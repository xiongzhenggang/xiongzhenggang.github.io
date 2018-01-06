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
2. 安装完成后，配置集群策略
```
# 集群配置文件
[root@localhost redis-4.0.6]# mkdir redis-cluster/7000
[root@localhost redis-4.0.6]# mkdir redis-cluster/7001
[root@localhost redis-4.0.6]# mkdir redis-cluster/7002
[root@localhost redis-4.0.6]# mkdir redis-cluster/7003
[root@localhost redis-4.0.6]# mkdir redis-cluster/7004
[root@localhost redis-4.0.6]# mkdir redis-cluster/7005
# 操被基础配置文件到创建的目录下
cp redis.conf redis-cluster/7000
touch redis-server.out  redis-cluster/7000
#其他相同，并在每个文件下创建一个redis-server.out用于查看启动日志
```
3. 配置集群中的每一个redis service
```
[root@localhost redis-4.0.6]# vi redis-cluster/7000/redis.conf
--------- 主要修改如下内容，其他redis service配置参照------------
port 7000 #在不同的服务器和nodes-xx中，端口也不同
cluster-enabled yes
bind 0.0.0.0
# daemonize yes #redis后台运行
cluster-config-file nodes-7000.conf
cluster-node-timeout 5000
appendonly yes
```
4. 配置完成后依次启动

```sh
src/redis-server redis-cluster/7000/redis.conf
#或者后台启动
nohup src/redis-server redis-cluster/7000/redis.conf > redis-cluster/7000/redis-server.out 2>&1 &
```
5. 创建集群
