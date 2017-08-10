## 接着可以通过devlopment来更新自己的应用，从版本v1-->版本v2

* 为了更新镜像应用从v1到v2，需要用到set image命令后面跟上 deployment 的名字和新版本的名字
```sh
kubectl set image deployments/kubernetes-bootcamp kubernetes-bootcamp=jocatalin/kubernetes-bootcamp:v2
```
* 注意 Deployment的作用
```
定义Deployment来创建Pod和ReplicaSet
滚动升级和回滚应用
扩容和缩容
暂停和继续Deployment
```
* 查看执行后的相关信息
```
[root@centos-master xzg]# kubectl get pods
NAME                                   READY     STATUS              RESTARTS   AGE
kubernetes-bootcamp-2100875782-jft0v   0/1       ContainerCreating   0          26s
kubernetes-bootcamp-2100875782-zc6mc   0/1       ContainerCreating   0          26s
kubernetes-bootcamp-390780338-1xxf3    0/1       Terminating         0          8m
kubernetes-bootcamp-390780338-2r748    1/1       Running             0          23h
kubernetes-bootcamp-390780338-9nrr4    1/1       Running             0          3d
```
* 其中会有状态标识镜像的拉取情况，是集群在拉取或等待。。
* 一段时间后在此执行查看状态
```
[root@centos-master xzg]# kubectl get pods
NAME                                   READY     STATUS    RESTARTS   AGE
kubernetes-bootcamp-2100875782-86840   1/1       Running   0          2m
kubernetes-bootcamp-2100875782-jft0v   1/1       Running   0          4m
kubernetes-bootcamp-2100875782-zc6mc   1/1       Running   0          4m
```
* 可以看到状态正常
```sh
# 创建环境变量（主机映射端口）
export NODE_PORT=$(kubectl get services/kubernetes-bootcamp -o go-template='{{(index .spec.ports 0).nodePort}}')
echo NODE_PORT=$NODE_PORT
```
* 查看
```
curl host01:$NODE_PORT
```
* 查看是否更新成功到v2
```sh
[root@centos-master xzg]# kubectl  get nodes
NAME              STATUS     AGE       VERSION
centos-minion-1   Ready      3d        v1.5.2
centos-minion-2   Ready      3d        v1.5.2
centos-minion-n   NotReady   3d        v1.5.2
minion-n          NotReady   3d        v1.5.2
[root@centos-master xzg]# curl -i centos-minion-2:$NODE_PORT
HTTP/1.1 200 OK
Content-Type: text/plain
Date: Sun, 06 Aug 2017 05:04:38 GMT
Connection: keep-alive
Transfer-Encoding: chunked

Hello Kubernetes bootcamp! | Running on: kubernetes-bootcamp-2100875782-86840 | v=2
[root@centos-master xzg]# curl -i centos-minion-1:$NODE_PORT
HTTP/1.1 200 OK
Content-Type: text/plain
Date: Sun, 06 Aug 2017 05:05:39 GMT
Connection: keep-alive
Transfer-Encoding: chunked

Hello Kubernetes bootcamp! | Running on: kubernetes-bootcamp-2100875782-jft0v | v=2
```
* 查看发布的状态
```sh
[root@centos-master xzg]# kubectl rollout status deployments/kubernetes-bootcamp
deployment "kubernetes-bootcamp" successfully rolled out

```
### 现在回滚到之前的版本
```sh
## 回滚
[root@centos-master xzg]# kubectl rollout undo  deployment/kubernetes-bootcamp
deployment "kubernetes-bootcamp" rolled back
## 回滚后查看
[root@centos-master xzg]# curl -i centos-minion-1:$NODE_PORT
HTTP/1.1 200 OK
Content-Type: text/plain
Date: Sun, 06 Aug 2017 05:26:59 GMT
Connection: keep-alive
Transfer-Encoding: chunked

Hello Kubernetes bootcamp! | Running on: kubernetes-bootcamp-390780338-wvvl8 | v=1
```
