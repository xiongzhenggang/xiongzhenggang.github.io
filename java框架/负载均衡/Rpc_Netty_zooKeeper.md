## 轻量级分布式 RPC 框架
### 使用的技术
1. Spring：它是最强大的依赖注入框架，也是业界的权威标准。
2. Netty：它使 NIO 编程更加容易，屏蔽了 Java 底层的 NIO 细节。
3. Protostuff：它基于 Protobuf 序列化框架，面向 POJO，无需编写 .proto 文件。
4. ZooKeeper：提供服务注册与发现功能，开发分布式系统的必备选择，同时它也具备天生的集群能力

* 第一步首先搭建zk环境

 具体参考![ZookPeeper简单使用步骤](https://github.com/xiongzhenggang/xiongzhenggang.github.io/blob/master/java框架/负载均衡/zookeeper_简单操作.md)
