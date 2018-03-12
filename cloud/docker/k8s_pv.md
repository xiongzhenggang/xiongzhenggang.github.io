# 在使用PV/PVC/StorageClass
* 具体区别可[k8s中文解释](https://www.kubernetes.org.cn/pvpvcstorageclass)，这里只阐述在k8s集群中如何使用功能namespace、quota以及pvc资源限额
1. pv和pvc的关系
```
PersistentVolume（PV）是集群中已由管理员配置的一段网络存储。 PV是类似卷之类的卷插件，是具有独立于使用PV的任何单个pod的生命周期。
pv的存储插件可使用NFS，iSCSI或云提供商特定的存储系统。
PersistentVolumeClaim（PVC）是用户存储的请求。 它类似于pod。 Pod消耗node，PVC消耗pv。 pod可以请求特定级别的资源（CPU和内存）。 虽然PersistentVolumeClaims允许用户使用抽象存储资源，但是常见的是，用户需要具有不同属性（如性能）的PersistentVolumes，用于不同的问题。
集群管理员需要能够提供多种不同于PersistentVolumes，而不仅仅是大小和访问模式，而不会使用户了解这些卷的实现细节。 对于这些需求，存在StorageClass资源。

StorageClass为管理员提供了一种描述他们提供的存储的“类”的方法。 不同的类可能映射到服务质量级别，或备份策略，或者由群集管理员确定的任意策略。
Kubernetes本身对于什么类别代表是不言而喻的。 这个概念有时在其他存储系统中称为“配置文件”
```
