# 第17种 责任链模式
### 责任链模式，有多个对象，每个对象持有对下一个对象的引用，这样就会形成一条链，请求在这条链上传递，直到某一对象决定处理该请求。但是发出者并不清楚到底最终那个对象会处理该请求，所以，责任链模式可以实现，在隐瞒客户端的情况下，对系统进行动态的调整。先看看关系图：
![责任链模式](/java23种设计模式/img/chain.png)
### Abstracthandler类提供了get和set方法，方便MyHandle类设置和修改引用对象，MyHandle类是核心，实例化后生成一系列相互持有的对象，构成一条链。
```java  
/**
 * @author xzg
 *	定义责任链持有的对象的接口
 */
interface Handler{
	public void opertator();
}
```
```java
/**
 * @author xzg
 * 为责任链模式提供共通处理方法
 */
abstract class Abstracthandler{
	private Handler handler;

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}
}
```
```java
class MyHandler extends Abstracthandler implements Handler{
	//用于标识该节点的名称
	private String name;
	public MyHandler(String name){
		this.name = name;
	}
	/**
	 * 节点操作
	 */
	public void opertator() {
		// TODO Auto-generated method stub
		System.out.println("本节点："+name);
		if(getHandler()!= null)
			getHandler().opertator();
	}
}
```
### 测试类
```java
public class ChainTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MyHandler my0 = new MyHandler("zero");
		MyHandler my1 = new MyHandler("first");
		MyHandler my2 = new MyHandler("second");
		//责任链模式流程核心，将对象通过类似链表的方式进行链接
		my0.setHandler(my1);
		my1.setHandler(my2);
		my0.opertator();
	}

}
```
### 强调一点就是，链接上的请求可以是一条链，可以是一个树，还可以是一个环，模式本身不约束这个，需要我们自己去实现，同时，在一个时刻，命令只允许由一个对象传给另一个对象，而不允许传给多个对象。
