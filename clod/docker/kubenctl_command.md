## 通过前面的配置这里介绍一些kubenctl命令行工具的使用
### 先说命令行的优缺点：
* 优点
* Commands are simple, easy to learn and easy to remember.
* Commands require only a single step to make changes to the cluster.
* 缺点
* Commands do not integrate with change review processes.
* Commands do not provide an audit trail associated with changes.
* Commands do not provide a source of records except for what is live.
* Commands do not provide a template for creating new objects
1. 运行一个调度类型的实例在容器中运行
```
# 1、可以执行使用run命令
kubectl run nginx --image nginx
# 2、可以直接使用create命令
kubectl create deployment nginx --image nginx
```
2. 使用yaml配置文件操作
```
#创建
kubectl create -f nginx.yaml
#删除
kubectl delete -f nginx.yaml -f redis.yaml
#替换
kubectl replace -f nginx.yaml
```
3. 对运行中的容器做些修改
```
# 如果一个容器已经在运行，这时需要对一些容器属性进行修改，又不想删除容器，或不方便通过replace的方式进行更新。kubernetes还提供了一种在容器运行时，直接对容器进行修改的方式，就是patch命令。 如创建pod的label是app=nginx-2，如果在运行过程中，需要把其label改为app=nginx-3，这patch命令如
kubectl patch pod rc-nginx-2-kpiqt -p '{"metadata":{"labels":{"app":"nginx-3"}}}'

```
