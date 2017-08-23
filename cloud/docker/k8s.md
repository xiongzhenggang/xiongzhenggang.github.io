## k8s替代swarm作为docker集群方案
参考地址[k8s官方地址](https://kubernetes.io/)</br>
* 具体步骤可参考[centos多机器使用案例](https://kubernetes.io/docs/getting-started-guides/centos/centos_manual_config/)
### 部分记录

* 设置yum源 
```
[virt7-docker-common-release]
name=virt7-docker-common-release
baseurl=http://cbs.centos.org/repos/virt7-docker-common-release/x86_64/os/
gpgcheck=0
```
* 安装yum -y install --enablerepo=virt7-docker-common-release kubernetes etcd flannel

* 为所有的主机添加域名解析功能
```
echo "192.168.5.240    centos-master
192.168.5.213    centos-minion-1
192.168.5.29  centos-minion-2" >> /etc/hosts
```
* 编辑/etc/kubernetes/config 设置master
KUBE_MASTER="--master=http://centos-master:8080"
* 关闭防火墙
```
setenforce 0
systemctl disable iptables-services firewalld
systemctl stop iptables-services firewalld
```
* 在master主机上设置下面设置服务返现etcd
1. 设置监听地址
```
# [member]
ETCD_NAME=default
ETCD_DATA_DIR="/var/lib/etcd/default.etcd"
ETCD_LISTEN_CLIENT_URLS="http://0.0.0.0:2379"

#[cluster]
ETCD_ADVERTISE_CLIENT_URLS="http://0.0.0.0:2379"
 ```
 2. 设置apiservice监听地址
```
# The address on the local server to listen to.
KUBE_API_ADDRESS="--address=0.0.0.0"

# The port on the local server to listen on.
KUBE_API_PORT="--port=8080"

# Port kubelets listen on
KUBELET_PORT="--kubelet-port=10250"

# Comma separated list of nodes in the etcd cluster
KUBE_ETCD_SERVERS="--etcd-servers=http://centos-master:2379"

# Address range to use for services
KUBE_SERVICE_ADDRESSES="--service-cluster-ip-range=10.254.0.0/16"

# Add your own!
KUBE_API_ARGS=""
```
3. 启动并设置网路
* Start ETCD and configure it to hold the network overlay configuration on master: Warning This network must be unused in your network infrastructure! 172.30.0.0/16 is free in our network.
```
systemctl start etcd
etcdctl mkdir /kube-centos/network
etcdctl mk /kube-centos/network/config "{ \"Network\": \"172.30.0.0/16\", \"SubnetLen\": 24, \"Backend\": { \"Type\": \"vxlan\" } }"
```
4. naster设置网路Configure flannel to overlay Docker network in /etc/sysconfig/flanneld on the master
```
# Flanneld configuration options

# etcd url location.  Point this to the server where etcd runs
FLANNEL_ETCD_ENDPOINTS="http://centos-master:2379"

# etcd config key.  This is the configuration key that flannel queries
# For address range assignment
FLANNEL_ETCD_PREFIX="/kube-centos/network"

# Any additional options that you want to pass
#FLANNEL_OPTIONS=""
```
5. master启动kubernets etcd flannel
```sh
for SERVICES in etcd kube-apiserver kube-controller-manager kube-scheduler flanneld; do
    systemctl restart $SERVICES
    systemctl enable $SERVICES
    systemctl status $SERVICES
done
```
6. maseter上configure the kubelet and start the kubelet and proxy(/etc/kubernetes/kubelet )
```
# The address for the info server to serve on
KUBELET_ADDRESS="--address=0.0.0.0"

# The port for the info server to serve on
KUBELET_PORT="--port=10250"

# You may leave this blank to use the actual hostname
# Check the node number!
KUBELET_HOSTNAME="--hostname-override=centos-minion-n"

# Location of the api-server
KUBELET_API_SERVER="--api-servers=http://centos-master:8080"

# Add your own!
KUBELET_ARGS=""
```
7. 在所有的节点上配置（flannel to overlay Docker network in /etc/sysconfig/flanneld ）
```
# Flanneld configuration options

# etcd url location.  Point this to the server where etcd runs
FLANNEL_ETCD_ENDPOINTS="http://centos-master:2379"

# etcd config key.  This is the configuration key that flannel queries
# For address range assignment
FLANNEL_ETCD_PREFIX="/kube-centos/network"

# Any additional options that you want to pass
#FLANNEL_OPTIONS=""
```
8. 在其他node节点上配置 /etc/kubernetes/kubelet(configure the kubelet and start the kubelet and proxy)
```
# The address for the info server to serve on
KUBELET_ADDRESS="--address=0.0.0.0"

# The port for the info server to serve on
KUBELET_PORT="--port=10250"

# You may leave this blank to use the actual hostname
# Check the node number!
KUBELET_HOSTNAME="--hostname-override=centos-minion-n"

# Location of the api-server
KUBELET_API_SERVER="--api-servers=http://centos-master:8080"

# Add your own!
KUBELET_ARGS=""
```
9. 在其他节点上启动Start the appropriate services 
```sh
for SERVICES in kube-proxy kubelet flanneld docker; do
    systemctl restart $SERVICES
    systemctl enable $SERVICES
    systemctl status $SERVICES
done
```
10. Configure kubectl
```
kubectl config set-cluster default-cluster --server=http://centos-master:8080
kubectl config set-context default-context --cluster=default-cluster --user=default-admin
kubectl config use-context default-context
```
* 好了可以测试下
```sh
kubectl get nodes
```
 接下来https://github.com/xiongzhenggang/xiongzhenggang.github.io/blob/master/clod/docker/k8s_deployed.md
