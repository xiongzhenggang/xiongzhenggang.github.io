## 使用私有镜像仓库
1. 开启两个虚拟机。 （1）192.168.5.230作为registry 192.168.5.231作为docker运行节点之一
2. 安装registry
##方法1
```sh
sudo yum install -y python-devel libevent-devel python-pip gcc xz-devel
```
其中可能出现的问题pip命令无法运行
因为cnetos的源更新较慢，所以可以安装扩展源来解决，如下命令
```sh
安装扩展源：sudo yum -y install epel-release

安装python-pip模块：sudo yum install python-pip
```
```sh
#更新：
pip install --upgrade pip
#安装仓库：
pip install docker-registry
```
## 方法2
```sh
直接拉去
docker run -d -v /home/xzg/registry:/var/lib/registry -p 5000:5000 --restart=always --name registry registry
##  /home/xzg/registry本机地址映射到docker容器中的/var/lib/registry
## 其他
sudo apt-get install build-essential python-dev libevent-dev python-pip libssl-dev liblzma-dev libffi-dev
git clone https://github.com/docker/distribution
cd distribution
sudo docker build .
```
2. 安装后可执行如下查看：
http://192.168.5.230:5000/v2/_catalog 结果{}
3. 用另一台主机测试上传拉去镜像
* 打tag标记
```sh
docker tag docker.io/swarm 192.168.5.230:5000/swarm
注意其中192.168.5.230:5000/swarm为的语法格式为 docker tag IMAGE[:TAG] [REGISTRYHOST/][USERNAME/]NAME[:TAG]
192.168.5.230:5000指仓库地址url和端口
```
* 推送
```sh
docker push 192.168.5.230:5000/swarm
```
* 删除本地镜像
```sh
docker rmi 192.168.5.230/swarm
```
* 远程拉取
```sh
docker pull 192.168.5.230:5000/swarm
```
查看
```sh
docker images
```

