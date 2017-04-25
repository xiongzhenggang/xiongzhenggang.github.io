# 第18种 命令模式
### 命令模式很好理解，举个例子，司令员下令让士兵去干件事情，从整个事情的角度来考虑，司令员的作用是，发出口令，口令经过传递，传到了士兵耳朵里，士兵去执行。这个过程好在，三者相互解耦，任何一方都不用去依赖其他人，只需要做好自己的事儿就行，司令员要的是结果，不会去关注到底士兵是怎么实现的。我们看看关系图：
![命令模式](/java23种设计模式/img/command.png)
### Invoker是调用者（司令员），Receiver是被调用者（士兵），MyCommand是命令，实现了Command接口，持有接收对象，看实现代码
```java  
   /**
 * @author xzg
 * 命令接口
 */
interface Command{
	public void exec(String str);
}
```

```java 
/**
 * @author xzg
 *	定义命令实体，包含执行该命令的下属。
 */
class MyCommand implements Command{
	private Receiver receiver;
	/**@param receiver
	 */
	public MyCommand(Receiver receiver){
		this.receiver = receiver;
	}
```
```java 
/**
	 * @see com.xzg.design.Command#exec()
	 * 上级发出的命令的实际执者
	 */
	public void exec(String str) {
		// TODO Auto-generated method stub
		receiver.action(str);
	}
}
```
```java
/**
 * @author xzg
 *作为命令的接受执行者，定义执行方法
 */
class Receiver{
	/**
	 * 执行者执行的方法，可传递命令
	 */
	public void action(String str){
		System.out.println(this.toString()+"收到命令--执行："+str);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "小斥候";
	}
	
}
```
```java
/**
 * @author xzg
 * 命令的发出者并不知道谁是命令的执行者是谁。它只需要明确自己的命令即可。
 * 命令的发出者和执行者之间解耦，实现请求和执行分开
 */
class Invoker{
	private Command command;
	public Invoker(Command command){
		this.command = command;
	}
	/**
	 * @param str
	 * 调用命令去执行想要执行的内容
	 */
	public void action(String str) {
		command.exec(str);
	}
}

```
### 测试类：
```java
public class CommandTest {
	public static void main(String[] args){
		Receiver receiver = new Receiver();
		Command command = new MyCommand(receiver);
		Invoker invoker = new Invoker(command);
		invoker.action("明天去打猎！！");
	}
}
```
### 熟悉Struts的同学应该知道，Struts其实就是一种将请求和呈现分离的技术，其中必然涉及命令模式的思想！
