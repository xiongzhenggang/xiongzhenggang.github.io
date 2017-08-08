## 接着上回部署k8s集群
名词解释：
```
Deployment为Pod和Replica Set（下一代Replication Controller）提供声明式更新。

你只需要在Deployment中描述你想要的目标状态是什么，Deployment controller就会帮你将Pod和Replica Set的实际状态改变到你的目标状态。你可以定义一个全新的Deployment，也可以创建一个新的替换旧的Deployment。

一个典型的用例如下：

使用Deployment来创建ReplicaSet。ReplicaSet在后台创建pod。检查启动状态，看它是成功还是失败。
然后，通过更新Deployment的PodTemplateSpec字段来声明Pod的新状态。这会创建一个新的ReplicaSet，Deployment会按照控制的速率将pod从旧的ReplicaSet移动到新的ReplicaSet中。
如果当前状态不稳定，回滚到之前的Deployment revision。每次回滚都会更新Deployment的revision。
扩容Deployment以满足更高的负载。
暂停Deployment来应用PodTemplateSpec的多个修复，然后恢复上线。
根据Deployment 的状态判断上线是否hang住了。
清除旧的不必要的ReplicaSet。
```
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
为了避免直接将应用暴露出去，创建路由代理，在master机器上执行
```sh
kubectl proxy
```
现在在另一台主机和集群创建完代理，在开一个master终端执行下面命令
```sh
export POD_NAME=$(kubectl get pods -o go-template --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}')
echo Name of the Pod: $POD_NAME
```
解释：
```
We now have a connection between our host (the online terminal) and the Kubernetes cluster. The started proxy enables direct access to the API. The app runs inside a Pod (we'll cover the Pod concept in next module). Get the name of the Pod and store it in the POD_NAME environment variable:
```
查看：
```
curl http://localhost:8001/api/v1/proxy/namespaces/default/pods/$POD_NAME/
```
* 这个url就是到pod的路由 可以看到一个名为kubernetes-bootcamp-390780338-9nrr4的pod

* 查看log
```sh
kubectl logs $POD_NAME #这里$POD_NAME为之前设置的环境变量
```
### 接下来可以在容器中执行命令
exec 命令可以集群中执行相应操作
例如：
```
[root@centos-master ~]# kubectl exec $POD_NAME env
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
HOSTNAME=kubernetes-bootcamp-390780338-9nrr4
KUBERNETES_PORT_443_TCP_PORT=443
KUBERNETES_PORT_443_TCP_ADDR=10.254.0.1
KUBERNETES_SERVICE_HOST=10.254.0.1
KUBERNETES_SERVICE_PORT=443
KUBERNETES_SERVICE_PORT_HTTPS=443
KUBERNETES_PORT=tcp://10.254.0.1:443
KUBERNETES_PORT_443_TCP=tcp://10.254.0.1:443
KUBERNETES_PORT_443_TCP_PROTO=tcp
NPM_CONFIG_LOGLEVEL=info
NODE_VERSION=6.3.1
HOME=/root
```
* 使用命令'kubectl exec -ti $POD_NAME bash'打开pod终端
```
[root@centos-master ~]# kubectl exec -ti $POD_NAME bash
root@kubernetes-bootcamp-390780338-9nrr4:/#ls
bin  boot  core  dev  etc  home  lib  lib64  media  mnt  opt  proc  root  run  sbin  server.js	srv  sys  tmp  usr  var
## 运行测试service.js如下：
root@kubernetes-bootcamp-390780338-9nrr4:/# curl localhost:8080
Hello Kubernetes bootcamp! | Running on: kubernetes-bootcamp-390780338-9nrr4 | v=1
```
* 接下来可以执行扩容操作
```sh
kubectl scale deployments/kubernetes-bootcamp --replicas=2
```
* 使用exit命令退出pod
接下来：https://github.com/xiongzhenggang/xiongzhenggang.github.io/blob/master/clod/docker/k8s_service.md
</br>
参考：https://kubernetes.io/docs/tutorials/kubernetes-basics/deploy-interactive/
