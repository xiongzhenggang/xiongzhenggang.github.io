## 接着上回部署k8s集群
* 接下来创建部署
1. 创建k8s部署平台 kubectl run 命令
```
kubectl run kubernetes-bootcamp --image=docker.io/jocatalin/kubernetes-bootcamp:v1 --port=8080
```
部署的作用有三点：
```
searched for a suitable node where an instance of the application could be run (we have only 1 available node)
scheduled the application to run on that Node
configured the cluster to reschedule the instance on a new Node when needed
```
使用命令查看创建的部署
 ```
kubectl get deployments
```

参考：https://kubernetes.io/docs/tutorials/kubernetes-basics/deploy-interactive/
