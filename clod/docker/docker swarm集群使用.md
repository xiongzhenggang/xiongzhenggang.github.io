## 关于使用swarm管理docker集群
1. 首先安装开启多个虚拟机安装docker，安装不做赘述
```sh
$ docker-machine create -d virtualbox manager
$ docker-machine create -d virtualbox agent1
$ docker-machine create -d virtualbox agent2
```
2. 创建发现服务的token如下命令
```sh
[root@localhost ~]# docker run --rm swarm create
Unable to find image 'swarm:latest' locally
Trying to pull repository docker.io/library/swarm ... 
latest: Pulling from docker.io/library/swarm
ad8c679cee1a: Pull complete 
97186f5f56a9: Pull complete 
821a304aaa0d: Pull complete 
Digest: sha256:1a05498cfafa8ec767b0d87d11d3b4aeab54e9c99449fead2b3df82d2744d345
Token based discovery is now deprecated and might be removed in the future.
It will be replaced by a default discovery backed by Docker Swarm Mode.
Other mechanisms such as consul and etcd will continue to work as expected.
259e8eec3ce0525e83d4f75878bf496d
```
命令解释如下：
* docker run 命令启动一个 Docker image,在本例中就是一个 swarm。如果本地没有则去远程仓库pull这里远程仓库为dockerhub
* -rm 参数 告诉 Docker 当docker停止的时候自动移除 （This command can be read: run the latest version of the swarm container, execute the create command, 
and, when the it completes, remove the swarm container from the local machine.）
* 执行后最后一行显示构建的token：259e8eec3ce0525e83d4f75878bf496d

3. 运行swarm 的manager和agent
启动swarm  Swarm manager 让后创建 代理加入到swarm集群中
```sh
docker run -d -p 3376:3376 -t -v ~/.docker/machine/machines/manager:/certs:ro swarm manage -H 0.0.0.0:3376

  --tlsverify
  --tlscacert=/certs/ca.pem
  --tlscert=/certs/server.pem
  --tlskey=/certs/server-key.pem
  token://259e8eec3ce0525e83d4f75878bf496d
  ```
  命令解释：
* -d (or --detach): 在后台运行 swarm 容器 并且打印 它的container ID 
* -t:分配一个 pseudo-TTY终端
* -p: 容器和主机端口映射 3376 
* -v: 挂载本地卷(~/.docker/machine/machines/manager) 指定在容器的本地 (/certs) 使用只读方式 (ro)

4. 运行agengt代理将他们加入到swarm集群中
----版本问题


















参考来源：http://www.javaworld.com/article/3094782/open-source-tools/open-source-java-projects-docker-swarm.html
