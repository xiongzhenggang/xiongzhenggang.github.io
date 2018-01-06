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
* 需要在redis-cluster(本机)服务器上安装gem redis (为redis-trib可以执行)，其他服务器不需要。
```
# 由于本机使用 CentOS7 yum库中ruby的版本支持到 2.0.0,可gem 安装redis需要最低是2.2.2，需要单独安装
1.安装RVM:
gpg2 --keyserver hkp://keys.gnupg.net --recv-keys D39DC0E3
# 下载rvm
curl -L get.rvm.io | bash -s stable
# find / -name rvm -print
source /usr/local/rvm/scripts/rvm
# 安装ruby
rvm install 2.3.4
#使用一个ruby版本
rvm use 2.3.3
#设置默认版本
rvm use 2.3.4 --default
#卸载一个已知版本
rvm remove 2.0.0
#查看当前版本
[root@localhost redis-4.0.6]# ruby --version
ruby 2.3.4p301 (2017-03-30 revision 58214) [x86_64-linux]
# 再次安装成功
gem install redis
```
6. 开始创建集群
```
# --replicas 1  意味着我们要为每个创建的master创造一个slave 
[root@localhost redis-4.0.6]# src/redis-trib.rb create --replicas 1 192.168.1.105:7000 192.168.1.105:7001 192.168.1.105:7002 192.168.1.105:7003 192.168.1.105:7004 192.168.1.105:7005
---------如下信息表示安装完成--------------------------------------------
>>> Creating cluster
>>> Performing hash slots allocation on 6 nodes...
Using 3 masters:
192.168.1.105:7000
192.168.1.105:7001
192.168.1.105:7002
Adding replica 192.168.1.105:7003 to 192.168.1.105:7000
Adding replica 192.168.1.105:7004 to 192.168.1.105:7001
Adding replica 192.168.1.105:7005 to 192.168.1.105:7002
M: b922ed9f5f363ae1f0b1f2a1535ddacef28116ee 192.168.1.105:7000
   slots:0-5460 (5461 slots) master
M: 9ac354cfb2e0d47ab790f4a3aa73ac490efb83d9 192.168.1.105:7001
   slots:5461-10922 (5462 slots) master
M: 616e5769b2ee17217036276d654a735ed5444c42 192.168.1.105:7002
   slots:10923-16383 (5461 slots) master
S: 98ea1a89bc129882782452dcf863bd8061cc01e5 192.168.1.105:7003
   replicates b922ed9f5f363ae1f0b1f2a1535ddacef28116ee
S: ef1546a761013231081a1700f9058d30cb5dfb39 192.168.1.105:7004
   replicates 9ac354cfb2e0d47ab790f4a3aa73ac490efb83d9
S: c1697b78756c29603f228dc7433420c4543f866e 192.168.1.105:7005
   replicates 616e5769b2ee17217036276d654a735ed5444c42
Can I set the above configuration? (type 'yes' to accept): yes
>>> Nodes configuration updated
>>> Assign a different config epoch to each node
>>> Sending CLUSTER MEET messages to join the cluster
Waiting for the cluster to join.....
>>> Performing Cluster Check (using node 192.168.1.105:7000)
M: b922ed9f5f363ae1f0b1f2a1535ddacef28116ee 192.168.1.105:7000
   slots:0-5460 (5461 slots) master
   1 additional replica(s)
S: 98ea1a89bc129882782452dcf863bd8061cc01e5 192.168.1.105:7003
   slots: (0 slots) slave
   replicates b922ed9f5f363ae1f0b1f2a1535ddacef28116ee
S: ef1546a761013231081a1700f9058d30cb5dfb39 192.168.1.105:7004
   slots: (0 slots) slave
   replicates 9ac354cfb2e0d47ab790f4a3aa73ac490efb83d9
M: 616e5769b2ee17217036276d654a735ed5444c42 192.168.1.105:7002
   slots:10923-16383 (5461 slots) master
   1 additional replica(s)
M: 9ac354cfb2e0d47ab790f4a3aa73ac490efb83d9 192.168.1.105:7001
   slots:5461-10922 (5462 slots) master
   1 additional replica(s)
S: c1697b78756c29603f228dc7433420c4543f866e 192.168.1.105:7005
   slots: (0 slots) slave
   replicates 616e5769b2ee17217036276d654a735ed5444c42
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.
```
* 上述信息表示完成，以下部分测试
7. 测试集群是否可用
```
[root@localhost redis-4.0.6]# src/redis-cli -c -p 7000
127.0.0.1:7000> set foo bar
-> Redirected to slot [12182] located at 192.168.1.105:7002
OK
192.168.1.105:7002> get foo
"bar"
192.168.1.105:7002> exit
[root@localhost redis-4.0.6]# src/redis-cli -c -p 7001
127.0.0.1:7001> get foo
-> Redirected to slot [12182] located at 192.168.1.105:7002
"bar"
192.168.1.105:7002> exit
```
