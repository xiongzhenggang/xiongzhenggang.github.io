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
