## 在做socket客户端链接服务端时，有时候会遇到获取不到输入或输出流的情况。
看下面两端代码：
代码1
```java
Socket socket=new Socket(地址，端口)
 
OutputStream os = socket.getOutputStream();
//输出返回值os.write();
os.close();
 
InputStream is = socket.getInputStream();
//接收输入流操作
is.close();
 
socket.close();
```
代码2
```java
Socket socket=new Socket(地址，端口)
 
OutputStream os = socket.getOutputStream();
//输出返回值os.write();
  
InputStream is = socket.getInputStream();
//接收输入流操作
  
is.close();
os.close();
socket.close();
```
### 可能这两种代码看上去区别不大唯一区别就是输入输出流的关闭顺序。而这种顺序不同也会导致出错。
* 代码1会出现无法获得输出流的错误信息，原因就是tcp协议是全双工通信，当一个流关闭了那么socket也无法正常工作了。
具体还要了解tcp协议相关的内容，这里就不多说。
### socket源码又如下描述：
This class implements server sockets. A server socket waits for requests to come in over the network. It performs some operation based on that request, and then possibly returns a result to the requester.

