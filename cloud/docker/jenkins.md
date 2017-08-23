### Jenkins作为一款优秀的持续性集成的软件在devlops中使用很广发，这里简单介绍其使用方式
1. 首先下载Jenkins[官方下载地址及教程](https://jenkins.io/download/)
```
方法一  下载war包打开终端到指定的下载的目录运行  java -jar jenkins.war --httpPort=8080
方法二  下载rpm包后安装运行rpm -ivh jenkins-2.70-1.1.noarch.rpm 。启动、关闭、重启service jenkins start/stop/restart。关闭防火墙
firewall-cmd --zone=public --add-port=8080/tcp --permanent
firewall-cmd --zone=public --add-service=http --permanent
firewall-cmd --reload
打开浏览器输入 http://安装机器ip:8080
图形界面操作使用
```
2. 开始使用：


