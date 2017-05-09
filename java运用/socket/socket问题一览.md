1.java.net.SocketTimeoutException.这个异常比较常见，socket超时。一般有2个地方会抛出这个，一个是connect的时候，这个超时参数由connect(SocketAddress endpoint,int timeout)中的后者来决定，还有就是setSoTimeout(int timeout)，这个是设定读取的超时时间。它们设置成0均表示无限大。

2.java.net.BindException:Address already in use: JVM_Bind。该异常发生在服务器端进行new ServerSocket(port) 或者socket.bind(SocketAddress bindpoint)操作时。
原因:与port一样的一个端口已经被启动，并进行监听。此时用netstat –an命令，可以看到一个Listending状态的端口。只需要找一个没有被占用的端口就能解决这个问题。
 
3.java.net.ConnectException: Connection refused: connect。该异常发生在客户端进行new Socket(ip, port)或者socket.connect(address,timeout)操作时.
原因:指定ip地址的机器不能找到（也就是说从当前机器不存在到指定ip路由），或者是该ip存在，但找不到指定的端口进行监听。应该首先检查客户端的ip和port是否写错了，假如正确则从客户端ping一下服务器看是否能ping通，假如能ping通（服务服务器端把ping禁掉则需要另外的办法），则看在服务器端的监听指定端口的程序是否启动。
 
4.java.net.SocketException: Socket is closed，该异常在客户端和服务器均可能发生。异常的原因是己方主动关闭了连接后（调用了Socket的close方法）再对网络连接进行读写操作。
 
5.java.net.SocketException: Connection reset或者Connect reset by peer:Socket write error。该异常在客户端和服务器端均有可能发生，引起该异常的原因有两个，第一个就是假如一端的Socket被关闭（或主动关闭或者因为异常退出而引起的关闭），另一端仍发送数据，发送的第一个数据包引发该异常(Connect reset by peer)。另一个是一端退出，但退出时并未关闭该连接，另一端假如在从连接中读数据则抛出该异常（Connection reset）。简单的说就是在连接断开后的读和写操作引起的。
对于服务器，一般的原因可以认为：
a) 服务器的并发连接数超过了其承载量，服务器会将其中一些连接主动Down掉.
b) 在数据传输的过程中，浏览器或者接收客户端关闭了，而服务端还在向客户端发送数据。
 
6.java.net.SocketException: Broken pipe。该异常在客户端和服务器均有可能发生。在抛出SocketExcepton:Connect reset by peer:Socket write error后，假如再继续写数据则抛出该异常。前两个异常的解决方法是首先确保程序退出前关闭所有的网络连接，其次是要检测对方的关闭连接操作，发现对方关闭连接后自己也要关闭该连接。
对于4和5这两种情况的异常，需要特别注意连接的维护。在短连接情况下还好，如果是长连接情况，对于连接状态的维护不当，则非常容易出现异常。基本上对长连接需要做的就是：
a) 检测对方的主动断连（对方调用了Socket的close方法）。因为对方主动断连，另一方如果在进行读操作，则此时的返回值是-1。所以一旦检测到对方断连，则主动关闭己方的连接（调用Socket的close方法）。
b) 检测对方的宕机、异常退出及网络不通,一般做法都是心跳检测。双方周期性的发送数据给对方，同时也从对方接收“心跳数据”，如果连续几个周期都没有收到对方心跳，则可以判断对方或者宕机或者异常退出或者网络不通，此时也需要主动关闭己方连接；如果是客户端可在延迟一定时间后重新发起连接。虽然Socket有一个keep alive选项来维护连接，如果用该选项，一般需要两个小时才能发现对方的宕机、异常退出及网络不通。

7.java.net.SocketException: Too many open files
原因: 操作系统的中打开文件的最大句柄数受限所致，常常发生在很多个并发用户访问服务器的时候。因为为了执行每个用户的应用服务器都要加载很多文件(new一个socket就需要一个文件句柄)，这就会导致打开文件的句柄的缺乏。
解决方式：
a) 尽量把类打成jar包，因为一个jar包只消耗一个文件句柄，如果不打包，一个类就消耗一个文件句柄。 

b) Java的GC不能关闭网络连接打开的文件句柄，如果没有执行close()则文件句柄将一直存在，而不能被关闭。也可以考虑设置socket的最大打开数来控制这个问题。对操作系统做相关的设置，增加最大文件句柄数量。ulimit -a可以查看系统目前资源限制，ulimit -n 10240则可以修改，这个修改只对当前窗口有效。
