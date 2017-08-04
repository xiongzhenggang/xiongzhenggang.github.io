## 接着上回在开始在service上向外暴露自己的应用
名词解释：
```
Kubernete Service 是一个定义了一组Pod的策略的抽象，我们也有时候叫做宏观服务。这些被服务标记的Pod都是（一般）通过label Selector决定的（下面我们会讲到我们为什么需要一个没有label selector的服务）

举个例子，我们假设后台是一个图形处理的后台，并且由3个副本。这些副本是可以相互替代的，并且前台并需要关心使用的哪一个后台Pod，当这个承载前台请求的pod发生变化时，前台并不需要直到这些变化，或者追踪后台的这些副本，服务是这些去耦

对于Kubernete原生的应用，Kubernete提供了一个简单的Endpoints API，这个Endpoints api的作用就是当一个服务中的pod发生变化时，Endpoints API随之变化，对于哪些不是原生的程序，Kubernetes提供了一个基于虚拟IP的网桥的服务，这个服务会将请求转发到对应的后台pod
```
```
Labels:标签其实就一对 key/value ，被关联到对象上，比如Pod,标签的使用我们倾向于能够标示对象的特殊特点，并且对用户而言是有意义的
Label选择器:与name和UID不同，label不提供唯一性。通常，我们会看到很多对象有着一样的label。

通过label选择器，客户端/用户能方便辨识出一组对象。label选择器是kubernetes中核心的组织原语。

API目前支持两种选择器：基于相等的和基于集合的。一个label选择器一可以由多个必须条件组成，由逗号分隔。在多个必须条件指定的情况下，所有的条件都必须满足，因而逗号起着AND逻辑运算符的作用。

一个空的label选择器（即有0个必须条件的选择器）会选择集合中的每一个对象。

一个null型label选择器（仅对于可选的选择器字段才可能）不会返回任何对象
```
这回主要三点
1. 学习k8s集群上使用service
2. 使用标签选择器关联service
3. 使用service暴露自己的应用在k8s集群上

Kubernete Service 是一个定义了一组Pod的策略的抽象，我们也有时候叫做宏观服务。这些被服务标记的Pod都是（一般）通过label Selector决定的
### 开始caozuo
* 创建一个新的Service
```sh
# 查看
kubectl get services
# 创建
kubectl expose deployment/kubernetes-bootcamp --type="NodePort" --port 8080
# 查看
kubectl get services
# 详细描述
kubectl describe services/kubernetes-bootcamp
# 创建一个环境变量
export NODE_PORT=$(kubectl get services/kubernetes-bootcamp -o go-template='{{(index .spec.ports 0).nodePort}}')
echo NODE_PORT=$NODE_PORT
# 测试一下
curl host01:$NODE_PORT
# Deployment为Pod和Replica Set（下一代Replication Controller）提供声明式更新。
kubectl describe deployment
# 
kubectl get pods -l run=kubernetes-bootcamp
# 设置环境变量
export POD_NAME=$(kubectl get pods -o go-template --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}')
echo Name of the Pod: $POD_NAME
# 设置标签
kubectl label pod $POD_NAME app=v1
# 详情
kubectl describe pods $POD_NAME
# 删除kubernetes-bootcamp service
kubectl delete service -l run=kubernetes-bootcamp
# 确认是否移除了相应的Service
curl host01:$NODE_POR
# 移除后确认应用是否还在
kubectl exec -ti $POD_NAME curl localhost:8080
```
