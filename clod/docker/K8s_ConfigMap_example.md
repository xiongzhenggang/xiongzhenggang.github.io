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

[k8s官网教程](https://kubernetes.io/docs/tutorials/configuration/configure-redis-using-configmap)
