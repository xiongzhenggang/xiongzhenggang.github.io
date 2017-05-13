
## 参考自制证书
![自制https证书](http://www.cnblogs.com/xinzhao/p/4950689.html)
## 证书颁发机构:

获取CA机构私钥：
获取CA机构私钥，使用命令：
### openssl genrsa -out ca.key 2048
 CA证书，命令：
### openssl req -x509 -new -key ca.key -out ca.crt
* 注意生成过程中需要输入一些CA机构的信息
## 服务端
### openssl genrsa -out server.key 2048
### openssl req -new -key server.key -out server.csr
* 注意生成过程中需要你输入一些服务端信息
*使用CA证书生成服务端证书
### openssl x509 -req -sha256 -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial -days 3650 -out server.crt
* 关于sha256，默认使用的是sha1，在新版本的chrome中会被认为是不安全的，因为使用了过时的加密算法。

* 打包服务端的资料为pkcs12格式(非必要，只是换一种格式存储上一步生成的证书)
### openssl pkcs12 -export -in server.crt -inkey server.key -out server.pkcs12
* 生成过程中，需要创建访问密码，请记录下来。
* 生成服务端的keystore（.jks文件, 非必要，Java程序通常使用该格式的证书）
### keytool -importkeystore -srckeystore server.pkcs12 -destkeystore server.jks -srcstoretype pkcs12
* 生成过程中，需要创建访问密码，请记录下来。
* 把ca证书放到keystore中（非必要）

### keytool -importcert -keystore server.jks -file ca.crt

## 客户端
 导入根证书ca.crt到浏览器受信任的根证书颁发机构列表中,不管通过什么浏览器吧，总之你要找到下面这个页面，点击导入，将上面生成的CA机构的ca.crt导入到收信任的根证书颁发机构列表中。
证书列表：

![证书列表](/web相关/img/CaFlow.png)

## Spring Boot
* Spring Boot为web容器提供了统一的抽象配置，不管你使用的是Tomcat是Jetty还是其它web容器，如果要在Spring Boot中使用Https，你只需要在你的配置类中，添加如下代码，注册一个EmbeddedServletContainerCustomizer Bean即可。

### 需要用到上面生成的Server.jks文件。

```java
@Configuration
public class WebConfig {

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        return new EmbeddedServletContainerCustomizer() {
            @Override
            public void customize(ConfigurableEmbeddedServletContainer container) {
                Ssl ssl = new Ssl();
                ssl.setKeyStore("Server.jks");
                ssl.setKeyStorePassword("passwd");
                container.setSsl(ssl);
                container.setPort(8443);
            }
        };
    }
}
```
##Nginx
```xml
server {
    listen      127.0.0.1:443 ssl;
    
    ssl on;
    ssl_certificate Server.crt;
    ssl_certificate_key Server.key;

    #省略无关配置...  
}
```

1、 crt、jks、pkcs12都是用来保存证书的不同格式，不同的服务器软件可能会使用不同格式的证书文件。
2、 OpenSSl、Keytool都是可以用来生成Https证书的工具软件，其中OpenSSl功能更多更复杂，Keytool随JDK安装而安装。
3、 证书的格式是多样的，生成证书的软件工具有很多，不同服务器程序的配置方法不尽相同，要达成目的有很多种方法。所以，重要的是弄懂原理，而不是按照教程一步一步敲命令。
4、 跟白话Https一样，本文仍然没有介绍服务端怎么验证客户端，但如果你弄懂了原理，我想你已经可以自己去实现了。


