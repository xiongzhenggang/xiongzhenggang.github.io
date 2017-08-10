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
