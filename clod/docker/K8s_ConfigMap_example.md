## 一个使用configmap配置readis的例子
1. 首先需要一个搭建好的k8s集群，当然也可以使用Minikube搭建本机集群，这里可以参考[搭建k8s集群](https://github.com/xiongzhenggang/xiongzhenggang.github.io/blob/master/clod/docker/k8s.md)
2. configmap的用处以及几种创建方式

* ConfigMap用于保存配置数据的键值对，可以用来保存单个属性，也可以用来保存配置文件。ConfigMap跟secret很类似，但它可以更方便地处理不包含敏感信息的字符串

```sh
# 从key-value字符串创建ConfigMap
$ kubectl create configmap special-config --from-literal=special.how=very
configmap "special-config" created
$ kubectl get configmap special-config -o go-template='{{.data}}'
map[special.how:very]

# 从env文件创建
$ echo -e "a=b\nc=d" | tee config.env
a=b
c=d
$ kubectl create configmap special-config --from-env-file=config.env
configmap "special-config" created
$ kubectl get configmap special-config -o go-template='{{.data}}'
map[a:b c:d]

# 从目录创建
$ mkdir config
$ echo a>config/a
$ echo b>config/b
$ kubectl create configmap special-config --from-file=config/
configmap "special-config" created
$ kubectl get configmap special-config -o go-template='{{.data}}'
map[a:a
 b:b
]
```
3. 开始redis的相关配置工作
```sh
# 创建目录及文件
# 创建configmap
kubectl create configmap example-redis-config --from-file=/home/xzg/configmap/redis/redis-config
# 查看yaml文件
kubectl get configmap example-redis-config -o yaml
```
* configmap yaml文件
```yaml
apiVersion: v1
data:
  redis-config: |
    maxmemory 2mb
    maxmemory-policy allkeys-lru
kind: ConfigMap
metadata:
  creationTimestamp: 2017-08-07T15:09:58Z
  name: example-redis-config
  namespace: default
  resourceVersion: "539510"
  selfLink: /api/v1/namespaces/default/configmaps/example-redis-config
  uid: 7a049260-7b82-11e7-9d65-08002708ab22
  ```
  3. 创建一个pod使用配置文件存储在configmap中
  * 通过pod的yaml创建pod
  ```yaml
  apiVersion: v1
kind: Pod
metadata:
  name: redis
spec:
  containers:
  - name: redis
    image: kubernetes/redis:v1
    env:
    - name: MASTER
      value: "true"
    ports:
    - containerPort: 6379
    resources:
      limits:
        cpu: "0.1"
    volumeMounts:
    - mountPath: /redis-master-data
      name: data
    - mountPath: /redis-master
      name: config
  volumes:
    - name: data
      emptyDir: {}
    - name: config
      configMap:
        name: example-redis-config
        items:
        - key: redis-config
          path: redis.conf
```
创建pod
```sh
kubectl create -f docs/user-guide/configmap/redis/redis-pod.yaml
# 镜像拉取后查找/redis-master/redis.conf配置文件
```
4. 查看是否配置生效
```
kubectl exec -it redis redis-cli
127.0.0.1:6379> CONFIG GET maxmemory
1) "maxmemory"
2) "2097152"
127.0.0.1:6379> CONFIG GET maxmemory-policy
1) "maxmemory-policy"
2) "allkeys-lru"
```

[k8s官网教程](https://kubernetes.io/docs/tutorials/configuration/configure-redis-using-configmap)
