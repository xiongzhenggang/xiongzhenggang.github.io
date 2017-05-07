## rpc简单实现(参考网上案例)
*  首先理解rpc 即Remote Procedure Call（远程过程调用），说得通俗一点就是：调用远程计算机上的服务，就像调用本地服务一样。
* RPC 可基于 HTTP 或 TCP 协议。
* 本案使用简单的例传统的阻塞式 IO 以及java的动态代理。
首先编写服务接口
```java  
/**先定义一个接口，这个接口就是客户端向服务端发起调用所使用的接口*/
public interface EchoService {
	String echo(String request);
	String ech(String request);
}
```
接着实现该服务接口
```java 
//接口实现类
public class EchoServiceImple implements EchoService{

	public String echo(String request) {
		// TODO Auto-generated method stub
		return "echo:"+request;
	}

	public String ech(String request) {
		// TODO Auto-generated method stub
		return null;
	}
}

```
### 在使用java的动态代理中,核心是先下面代码，其中前面两个参数分别为，调用远程接口的加载器和class。第三个参数作为客户端调用服务度的核心部分，主要功能：
* 1、与服务端建立tcp链接。 
* 2、发送客户端要调用的全类名（作为服务端反射需要的类名）、方法名（服务端反射所需的方法参数）、方法的参数类型method.getParameterTypes()以及方法参数值args。最后接收服务端发送的执行结果。
* 3、服务端获取这些参数后，通过放射调用服务端的程序，得到的结果发送给客户端
#### 下面是使用动态代理的api
```java
Proxy.newProxyInstance(ClassLoader loader,
                                          Class<?>[] interfaces,
                                          InvocationHandler h)
```
下面为客户端(InvocationHandler具体实现)通信的处理代码如下：
```java
public class DynamicProxyHandler implements InvocationHandler {
	//this class is used to invoke proxyed instance
	//方法重写，返回服务端执行的结果
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Socket s = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            s = new Socket();
            s.connect(new InetSocketAddress("localhost", 8081));
            oos = new ObjectOutputStream(s.getOutputStream());
            ois = new ObjectInputStream(s.getInputStream());
            //发送字符串
            oos.writeUTF("com.xzg.rpc.EchoServiceImple");//send first 
            oos.writeUTF(method.getName());//send second
            //发送对象类型的数据
            oos.writeObject(method.getParameterTypes());//发送客户端调用方法一些列的参数
            oos.writeObject(args);
            //阻塞读取服务端发送的数据
           Object object = ois.readObject();//返回服务端执行结果
            return object;//read service,block 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (s != null)
                s.close();

            if (ois != null)
                ois.close();

            if (oos != null)
                oos.close();
        }
        return null;
    }
}

```
* 服务端代码：
```java
/**服务端有新的客户端连接进来的时候，就从客户端那里读取要调用的类名，
 * 方法名，然后通过反射找到并且调用这个方法，然后再把调用结果发送给客户端。*/
public class RpcPublisher {
	 private  static Logger logger = Logger.getLogger(RpcPublisher.class);
	@SuppressWarnings("resource")
	public static void main(String args[]) {
		 PropertyConfigurator.configure( "/home/xzg/java/workspace/netty_chat/src/log4j.properties" );
		//网络对象序列化传递
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        Socket clientSocket = null;
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(8081);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                clientSocket = ss.accept();//监听
                ois = new ObjectInputStream(clientSocket.getInputStream());
                oos = new ObjectOutputStream(clientSocket.getOutputStream());
                //获取客户端写入的
                String serviceName = ois.readUTF();
                String methodName = ois.readUTF();
                logger.info("serviceName:"+serviceName+"=="+"methodName:"+methodName);
              //从客户端writeObject的对象,parameterTypes的参数类型
                Class<?>[] parameterTypes = (Class<?>[]) ois.readObject();
                Object[] parameters = (Object[]) ois.readObject();
                logger.info("parameterTypes.getClass():"+parameterTypes.getClass());
                logger.info("parameters.toString():"+parameters);
                Class<?> service = Class.forName(serviceName);
                logger.info("service.toString():"+service.toString());
                //获取客户端发送的代理对象，服务段执行相应的方法后，将数据发送会客户端
                Method method = service.getMethod(methodName, parameterTypes);
                //parameters:the arguments used for the method call
                oos.writeObject(method.invoke(service.newInstance(), parameters));
            } catch (Exception e) {
            	e.printStackTrace();
            } finally {
                if (oos != null) {
                    try {
                        oos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (clientSocket != null) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

```
### 最后是客户端的代理调用程序，具体如下：
```java
/**把socket通信都隐藏起来，让客户端只知道调用echo接口*/
public class Caller {
	public static void main(String args[]) {
		//动态代理执行
        EchoService echo = (EchoService)Proxy.newProxyInstance(EchoService.class.getClassLoader(),
                new Class<?>[]{EchoService.class}, new DynamicProxyHandler());
        for (int i = 0; i < 3; i++) {
            System.out.println(echo.echo(String.valueOf(i)));
        }
       // echo.ech("dd");
    }
}
```
