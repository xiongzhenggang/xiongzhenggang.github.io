## 容器的数据卷
* 下图展示容器和卷的关系
![容器主机和卷](/cloud/docker/image/types-of-mounts-volume.png)
1. 创建卷
* 有两种创建卷的标识 -v 或者 –mount 当然mount区别与v，就是他的参数更多
创建 volume:
```
$ docker volume create my-vol
```
展示 volumes:
```
$ docker volume ls
local               my-vol
```
volume详情:
```
$ docker volume inspect my-vol
[
    {
        "Driver": "local",
        "Labels": {},
        "Mountpoint": "/var/lib/docker/volumes/my-vol/_data",
        "Name": "my-vol",
        "Options": {},
        "Scope": "local"
    }
]
```
删除volume:
```
$ docker volume rm my-vol
```
2. 启动一个容器的时候添加参数来创建一个卷
```sh
	###使用--mount
$ docker run -d \
  -it \
  --name devtest \
  --mount source=myvol2,target=/app \
  nginx:latest
 ###或者使用-v
 $ docker run -d \
  -it \
  --name devtest \
  -v myvol2:/app \
  nginx:latest
```
查看一下详情：
```sh
$ docker inspect devtest
## 部分内容
"Mounts": [
    {
        "Type": "volume",
        "Name": "myvol2",
        "Source": "/var/lib/docker/volumes/myvol2/_data",
        "Destination": "/app",
        "Driver": "local",
        "Mode": "",
        "RW": true,
        "Propagation": ""
    }
],
```
3. 删除容器和卷
```sh
$ docker container stop devtest

$ docker container rm devtest

$ docker volume rm myvol2
```
4. 接下来挂载一个有数据的卷用于测试本地是否别挂载上了
```sh
### 使用--mount创建
$ docker run -d \
  -it \
  --name=nginxtest \
  --mount source=nginx-vol,destination=/usr/share/nginx/html \
  nginx:latest
  
### 或者使用-v创建
$ docker run -d \
  -it \
  --name=nginxtest \
  -v nginx-vol:/usr/share/nginx/html \
  nginx:latest
```
查看是否挂载上了
```sh
[root@localhost home]# ls /var/lib/docker/volumes/nginx-vol/_data/
50x.html  index.html
```
5. 挂载只读卷
```sh
### 使用--mount创建
$ docker run -d \
  -it \
  --name=nginxtest \
  --mount source=nginx-vol,destination=/usr/share/nginx/html,readonly \
  nginx:latest
  ### -v 略
```
清除
```sh
$ docker container stop nginxtest
$ docker container rm nginxtest
$ docker volume rm nginx-vol
```
6. 使用volum驱动
* 在使用docker volume create或者创建一个容器的时候创建volume的使用可以指定volume的驱动类型,比如vieux/sshfs
* 下面需要一些准备工作
```
首先需要两台主机，并且是可以ssh连接互通的，
在docker主机上安装vieux/sshfs插件
###
$ docker plugin install --grant-all-permissions vieux/sshfs
```
* 使用volume driver创建一个volume
```sh
## 如果两台主机共享相同的key配置可以省略密码，-o选项用于添加参数
$ docker volume create --driver vieux/sshfs \
  -o sshcmd=test@node2:/home/test \
  -o password=testpassword \
  sshvolume
```
* 也可以通过启动容器创建卷
```sh
## 本例指定ssh密码，如果两台主机共享key配置，可以省略。
## 如果卷驱动要求通过选项，则需要使用--mount
$ docker run -d \
  --it \
  --name sshfs-container \
  --volume-driver vieux/sshfs \
  --mount src=sshvolume,target=/app,volume-opt=sshcmd=test@node2:/home/test,volume-opt=password=testpassword \
  nginx:latest
```

[官方文档](https://docs.docker.com/engine/admin/volumes/volumes/#create-a-volume-using-a-volume-driver)