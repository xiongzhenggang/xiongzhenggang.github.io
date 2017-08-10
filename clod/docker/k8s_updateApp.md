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
